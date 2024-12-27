package org.example.SyncInteraction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.Review.BookReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SyncThreadBR implements Runnable
{
    final List<BookReview> revs;
    int ID;
    Document web;
    String mainURL, curURL;

    public SyncThreadBR(int idInput, List<BookReview> revsInput, String mainURLInput, String curURLInput)
    {
        ID = idInput;
        revs = revsInput;
        mainURL = mainURLInput;
        curURL = curURLInput;
    }

    @Override
    public void run()
    {
        System.out.println("Запуск потока номер " + (ID + 1));
        List<BookReview> pageRevs = getReviewsBooksFromPage();
        synchronized (revs)
        {
            revs.addAll(pageRevs);
            toGsonFile(revs, "gsonOutputBR.json");
        }
    }

    public List<BookReview> getReviewsBooksFromPage()
    {
        List<BookReview> revs = new ArrayList<>();

        try
        {
            web = Jsoup.connect(curURL).get();
            Elements revsParse = web.body().select("div[class=\"col-12\"]>div[class=\"universal-blocks\"]");
            if (revsParse.isEmpty()) return revs;

            for (Element review : revsParse)
            {
                BookReview temp = new BookReview();
                temp.setBookTitle(review.select("a[href^=\"/book.\"]").text());

                Elements bookAuthorsParse = review.select("a[href^=\"/bookauthor.\"]");
                List<String> bookAuthors = new ArrayList<>();
                for (Element el : bookAuthorsParse)
                {
                    bookAuthors.add(el.text());
                }
                temp.setBookAuthors(bookAuthors);
                temp.setRevAuthor(review.select(".link").text());

                try
                {
                    URL picURL = new URL(review.select("img").attr("src"));
                    temp.setCoverURL(picURL);

                    downloadImage(picURL, temp.getBookTitle());
                }
                catch (MalformedURLException e)
                {
                    System.out.println(e.getMessage());
                }

                try
                {
                    String textURL = review.select(".universal-blocks-content a[href^=\"/discussion.\"]").attr("href");
                    int tempID = textURL.indexOf('#');
                    String comment = textURL.substring(tempID).trim();

                    Document textDoc = Jsoup.connect(mainURL + textURL).get();
                    String cssQuery = "div" + comment +  " div[class=\"comment-content\"]";
                    String revText = textDoc.body().select(cssQuery).text();
                    temp.setRevText(revText);
                }
                catch (IOException e)
                {
                    System.out.println(e.getMessage());
                }

                String stars = review.select("div[class^=\"rating\"]").attr("class").substring(20).trim();
                if (stars.isEmpty()) temp.setRating(0);
                else temp.setRating(Integer.parseInt(stars));

                revs.add(temp);
            }

        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return revs;
    }

    private static void downloadImage(URL imgURL, String filename)
    {
        byte[] buffer = new byte[4096];
        int n = -1;

        try (InputStream in = imgURL.openStream())
        {
            OutputStream os = new FileOutputStream( "src\\main\\resources\\images" + "\\" + filename + ".jpg");
            while ((n = in.read(buffer)) != -1 )
            {
                os.write(buffer, 0, n);
            }
            os.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void toGsonFile(List<BookReview> bookRevs, String filename)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filename))
        {
            gson.toJson(bookRevs, writer);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
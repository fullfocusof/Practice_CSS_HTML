package org.example.HTMLInteraction;

import lombok.Getter;
import org.example.OnlineStore.OnlineStore;
import org.example.Review.BookReview;

import org.example.Review.OSReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTMLInteraction
{
    private static HTMLInteraction instance;
    @Getter
    String mainURL, curURL;
    Document web;

    private HTMLInteraction(String webURLInput)
    {
        curURL = webURLInput;
        mainURL = getBaseUrl(webURLInput);
    }

    public static HTMLInteraction getInstance(String webURLInput)
    {
        if (instance == null) instance = new HTMLInteraction(webURLInput);
        return instance;
    }

    public static String getBaseUrl(String url)
    {
        if (url.startsWith("http://")) return url.substring(0, url.indexOf("/", 7));
        else if (url.startsWith("https://")) return url.substring(0, url.indexOf("/", 8));
        else return url;
    }

    private static void downloadImage(URL imgURL, String filename)
    {
        byte[] buffer = new byte[2048];
        int n = -1;
        String URLStr = imgURL.toString();
        String extension = URLStr.substring(URLStr.lastIndexOf("."));

        try (InputStream in = imgURL.openStream())
        {
            OutputStream os = new FileOutputStream( "src\\main\\resources\\images" + "\\" + filename + extension);
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

    public List<BookReview> getReviewsBooks(int pagesMax)
    {
        List<BookReview> revs = new ArrayList<>();

        int pages = 0;
        while(pages < pagesMax)
        {
            try
            {
                web = Jsoup.connect(curURL).get();
                Elements revsParse = web.body().select("div[class=\"col-12\"]>div[class=\"universal-blocks\"]");
                if (revsParse.isEmpty()) break;

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
                        String picUrlStr = review.select("img").attr("src");
                        URL picURL;
                        if (picUrlStr.equals("/images/bookmix_cover.png")) picURL = new URL(mainURL + picUrlStr);
                        else picURL = new URL(picUrlStr);
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

            String nextPage = web.body().select("li.arrow a:has(i[class='fas fa-angle-right'])").attr("href");
            curURL = mainURL + nextPage;
            pages++;
        }

        return revs;
    }

    public List<OnlineStore> getMin50ReviewsStores()
    {
        List<OnlineStore> result = new ArrayList<>();

        try
        {
            web = Jsoup.connect(curURL).get();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        int pages = 0;
        while(pages < 5)
        {
            Elements storesParse = web.body().select("div.find-list-box");
            if (storesParse.isEmpty()) return new ArrayList<>();

            for (Element store : storesParse)
            {
                int cntRevs = Integer.parseInt(store.select("span.num").text());
                if (cntRevs >= 50)
                {
                    OnlineStore OS = new OnlineStore();
                    OS.setStoreName(store.select("a.ss").text().substring("Отзывы о ".length()));
                    OS.setAvgRating(Integer.parseInt(store.select("span.sro").text().substring("Средняя оценка - ".length())));
                    OS.setCntRevs(cntRevs);
                    List<OSReview> OSrevs = new ArrayList<>();
                    try
                    {
                        Document OSpage = Jsoup.connect(mainURL + store.select("a.ss").attr("href")).get();
                        Elements revsParse = OSpage.body().select("div[class=\"reviewers-box\"]");
                        if (revsParse.isEmpty()) break;

                        for (Element review : revsParse)
                        {
                            OSReview temp = new OSReview();
                            temp.setRevAuthor(review.select("span[itemprop=\"author\"]").text());
                            temp.setRating(Integer.parseInt(review.select("span[itemprop=\"ratingValue\"]").text()));
                            temp.setPros(review.select("td[itemprop=\"pro\"]").text());
                            temp.setCons(review.select("td[itemprop=\"contra\"]").text());
                            temp.setRevText(review.select("td[itemprop=\"reviewBody\"]").text());

                            OSrevs.add(temp);
                        }

                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }

                    OS.setRevs(OSrevs);
                    result.add(OS);
                }
            }

            String nextPage = web.body().select("a.next").attr("href");
            curURL = mainURL + nextPage;
            pages++;
        }

        return result;
    }

    public List<String> getPagesURL(int cnt)
    {
        List<String> result = new ArrayList<>();
        result.add(curURL);

        int pages = 0;
        while(pages < cnt - 1)
        {
            try
            {
                web = Jsoup.connect(curURL).get();
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

            String nextPage = web.body().select("li.arrow a:has(i[class='fas fa-angle-right'])").attr("href");
            curURL = mainURL + nextPage;
            result.add(curURL);
            pages++;
        }

        return result;
    }
}

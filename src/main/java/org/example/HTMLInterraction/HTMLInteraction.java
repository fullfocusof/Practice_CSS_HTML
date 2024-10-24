package org.example.HTMLInterraction;

import org.example.Review.Review;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HTMLInteraction
{
    private static HTMLInteraction instance;
    Document web;

    private HTMLInteraction(String webURLInput)
    {
        try
        {
            web = Jsoup.connect(webURLInput).get();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static HTMLInteraction getInstance(String webURLInput)
    {
        if (instance == null) instance = new HTMLInteraction(webURLInput);
        return instance;
    }

    public List<String> getReviews()
    {
        List<Review> revs = new ArrayList<>();

        while(true)
        {
            Elements revsParse = web.select(".comment");
            if (revs.isEmpty()) break;

            for (Element review : revs)
            {
                String bookTitle = review.select(".book-title").text();

            }

//            try
//            {
//
//            }
//            catch (IOException e)
//            {
//                System.out.println(e.getMessage());
//            }
        }

        return revs;
    }
}

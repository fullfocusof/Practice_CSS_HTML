package org.example.SyncInteraction;

import org.example.OnlineStore.OnlineStore;
import org.example.Review.BookReview;
import org.example.Review.OSReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyncThreadOS
{
    String mainURL;

    public SyncThreadOS(String mainURLInput)
    {
        mainURL = mainURLInput;
    }

    public List<OnlineStore> getMin50ReviewsStores(Element store)
    {
        List<OnlineStore> result = new ArrayList<>();

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
                if (!revsParse.isEmpty())
                {
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
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

            OS.setRevs(OSrevs);
            result.add(OS);
        }

        return result;
    }
}
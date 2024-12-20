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
import java.util.concurrent.BlockingQueue;

public class SyncThreadOS implements Runnable
{
    String mainURL;

    public SyncThreadOS(String mainURLInput)
    {
        mainURL = mainURLInput;
    }

    @Override
    public void run()
    {
//        System.out.println("Запуск потока номер " + (ID + 1));
//        List<OnlineStore> threadStores = new ArrayList<>();
//        synchronized (storesParse)
//        {
//            try
//            {
//                while (true)
//                {
//                    Element store = queue.take();
//                    threadStores = getMin50ReviewsStores(store);
//                }
//            }
//            catch (InterruptedException e)
//            {
//                Thread.currentThread().interrupt();
//                System.out.println(e.getMessage());
//            }
//        }
//        synchronized (onlineStores)
//        {
//            onlineStores.addAll(threadStores);
//            //toGsonFile(onlineStores, "gsonOutputOS.json");
//        }
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
                        if (OSrevs.size() == 3) break; // LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
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
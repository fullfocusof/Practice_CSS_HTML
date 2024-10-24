package org.example;

import org.example.HTMLInterraction.HTMLInteraction;
import org.example.OnlineStore.OnlineStore;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
//        HTMLInteraction html = HTMLInteraction.getInstance("https://bookmix.ru/comments/");
//        List<Review> revs = html.getReviewsBooks();

        HTMLInteraction html = HTMLInteraction.getInstance("https://na-negative.ru/internet-magaziny");
        List<OnlineStore> stores = html.getMin50ReviewsStores();
        for (OnlineStore OS : stores)
        {
            System.out.println(OS.toString() + "\n\n");
        }
    }
}
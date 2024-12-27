package org.example;

import org.example.CSVInteraction.CSVInteraction;
import org.example.HTMLInteraction.HTMLInteraction;
import org.example.OnlineStore.OnlineStore;
import org.example.Review.BookReview;
import org.example.SyncInteraction.SyncThreadBR;
import org.example.SyncInteraction.SyncThreadOS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main
{
    public static void main(String[] args)
    {
        HTMLInteraction html;
        CSVInteraction csv = new CSVInteraction();
        List<BookReview> revs = new ArrayList<>();
        List<OnlineStore> stores = new ArrayList<>();

        Scanner sc = new Scanner(System.in);
        String userInput;
        System.out.print(">");

        boolean exitProg = false;
        while (!exitProg)
        {
            userInput = sc.nextLine();
            switch (userInput)
            {
                case "exit":
                {
                    exitProg = true;
                }
                break;

                case "help":
                {
                    String sb = String.format("%-50s %s%n", "bookmix", "Собрать все отзывы на https://bookmix.ru/comments/") +
                            String.format("%-50s %s%n", "bookmixSync", "Собрать все отзывы на https://bookmix.ru/comments/ в режиме многопоточности") +
                            String.format("%-50s %s%n", "nanegative", "Составить список магазинов с более чем 50-ю отзывами на https://nanegative.ru/internet-magaziny") +
                            String.format("%-50s %s%n", "nanegativeSync", "Составить список магазинов с более чем 50-ю отзывами на https://nanegative.ru/internet-magaziny в режиме многопоточности") +
                            String.format("%-50s %s%n", "bookmixCSV", "Сохранить в CSV формат отзывы с https://bookmix.ru/comments/") +
                            String.format("%-50s %s%n", "nanegativeWconditions", "Сохранить в CSV формат отзывы с https://nanegative.ru/internet-magaziny") +
                            String.format("%-50s %s%n", "exit", "Выйти из программы");
                    System.out.println(sb);
                }
                break;

                case "bookmix":
                {
                    String pageInput;
                    System.out.print("Укажите количество страниц: ");
                    pageInput = sc.next();
                    sc.nextLine();
                    int pages = Integer.parseInt(pageInput);

                    System.out.print("Пожалуйста, подождите...\n");
                    Instant start = Instant.now();

                    html = HTMLInteraction.getInstance("https://bookmix.ru/comments/");
                    revs = html.getReviewsBooks(pages);

                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);

                    System.out.println("Время выполнения программы: " + (duration.toMillis()) + " мс");
                    System.out.println("Количество отзывов: " + revs.size());
                }
                break;

                case "nanegative":
                {
                    System.out.print("Пожалуйста, подождите...\n");
                    Instant start = Instant.now();

                    html = HTMLInteraction.getInstance("https://na-negative.ru/internet-magaziny");
                    stores = html.getMin50ReviewsStores();

                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);

                    System.out.println("Время выполнения программы: " + (duration.toMillis()) + " мс");

                    int cntRevs = 0;
                    for (OnlineStore OS : stores)
                    {
                        //System.out.println(OS.toString() + "\n\n");
                        cntRevs += OS.getRevs().size();
                    }
                    System.out.println("Количество отзывов: " + cntRevs);
                }
                break;

                case "bookmixSync":
                {
                    String pageInput;
                    System.out.print("Укажите количество страниц: ");
                    pageInput = sc.next();
                    sc.nextLine();
                    int pages = Integer.parseInt(pageInput);

                    System.out.print("Пожалуйста, подождите...\n");
                    Instant start = Instant.now();

                    html = HTMLInteraction.getInstance("https://bookmix.ru/comments/");
                    List<BookReview> revsSync = new ArrayList<>();
                    List<String> pagesURLRevs = html.getPagesURL(pages);

                    Thread[] ths = new Thread[pages];
                    int ID = 0;
                    for (String pageURL : pagesURLRevs)
                    {
                        Thread th = new Thread(new SyncThreadBR(ID, revsSync, html.getMainURL(), pageURL));
                        ths[ID] = th;
                        th.start();
                        ID++;
                    }

                    for (Thread th : ths)
                    {
                        try
                        {
                            th.join();
                        }
                        catch (InterruptedException e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }

                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);

                    System.out.println("Время выполнения программы: " + (duration.toMillis()) + " мс");
                    System.out.println("Количество отзывов: " + revsSync.size());
                }
                break;

                case "nanegativeSync":
                {
                    String threadsInput;
                    System.out.print("Укажите количество потоков: ");
                    threadsInput = sc.next();
                    sc.nextLine();
                    int threads = Integer.parseInt(threadsInput);

                    System.out.print("Пожалуйста, подождите...\n");
                    Instant start = Instant.now();

                    html = HTMLInteraction.getInstance("https://na-negative.ru/internet-magaziny");
                    Document web = null;
                    try
                    {
                        web = Jsoup.connect(html.getCurURL()).get();
                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    Elements storesParse = web.body().select("div.find-list-box");

                    SyncThreadOS thTest = new SyncThreadOS(html.getMainURL());
                    BlockingQueue<Element> queue = new ArrayBlockingQueue<>(storesParse.size(), true, storesParse);
                    ExecutorService executorService = Executors.newFixedThreadPool(threads);

                    List<OnlineStore> storesSync = Collections.synchronizedList(new ArrayList<>());;

                    for (int i = 0; i < threads; i++)
                    {
                        executorService.submit(() ->
                        {
                            try
                            {
                                while (!queue.isEmpty())
                                {
                                    Element store = queue.take();
                                    storesSync.addAll(thTest.getMin50ReviewsStores(store));
                                }
                            }
                            catch (InterruptedException e)
                            {
                                Thread.currentThread().interrupt();
                                System.out.println(e.getMessage());
                            }
                        });
                    }

                    executorService.shutdown();
                    try
                    {
                        if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                        {
                            executorService.shutdownNow();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }

                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);

                    System.out.println("Время выполнения программы: " + (duration.toMillis()) + " мс");

                    int cntRevs = 0;
                    for (OnlineStore OS : storesSync)
                    {
                        //System.out.println(OS.toString() + "\n\n");
                        cntRevs += OS.getRevs().size();
                    }
                    System.out.println("Количество отзывов: " + cntRevs);
                }
                break;

                case "bookmixCSV":
                {
                    if (revs.isEmpty())
                    {
                        System.out.print("Данные отсутствуют");
                        break;
                    }

                    csv.BRTextAuthorRatingSaveToCSV(revs, "csvTextAuthorRating.csv");
                    csv.BRBooksRevsSaveToCSV(revs, "csvBooksRevs.csv");
                    csv.BRBooksMaxRateSaveToCSV(revs, "csvBooksMaxRate.csv");
                }
                break;

                case "nanegativeWconditions":
                {
                    if (stores.isEmpty())
                    {
                        System.out.print("Данные отсутствуют");
                        break;
                    }

                    csv.printOnlyProsOSRevs(stores);
                    csv.printOnlyLessThanTwoStarsOSRevs(stores);
                    csv.printThreeGroupsOSRevs(stores);
                }
                break;

                default:
                {
                    System.out.println("\"" + userInput + "\"" + " не является командой");
                }
                break;
            }

            if (!exitProg)
            {
                System.out.print(">");
            }
        }
    }
}
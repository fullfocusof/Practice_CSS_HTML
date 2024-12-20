package org.example.CSVInteraction;

import com.opencsv.CSVWriter;
import org.example.OnlineStore.OnlineStore;
import org.example.Review.BookReview;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CSVInteraction
{
    public void BRTextAuthorRatingSaveToCSV(List<BookReview> data, String filename)
    {
        List<String[]> refactorData = data.stream()
                .map(BR -> new String[]{BR.getRevAuthor(), BR.getRevText(), String.valueOf(BR.getRating())})
                .collect(Collectors.toList());

        try (CSVWriter writer = new CSVWriter(new FileWriter(filename)))
        {
            String[] header = {"Автор отзыва", "Текст отзыва", "Рейтинг"};
            writer.writeNext(header);

            for (String[] review : refactorData)
            {
                writer.writeNext(review);
            }

            System.out.println("Данные успешно записаны в файл " + filename);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void BRBooksRevsSaveToCSV(List<BookReview> data, String filename)
    {
        List<HashMap<List<String>, String>> refactorData = data.stream()
                .map(BR ->
                {
                    HashMap<List<String>, String> map = new HashMap<>();
                    map.put(BR.getBookAuthors(), BR.getBookTitle());
                    return map;
                })
                .collect(Collectors.toList());

        try (CSVWriter writer = new CSVWriter(new FileWriter(filename)))
        {
            String[] header = {"Авторы книги", "Название книги"};
            writer.writeNext(header);

            for (HashMap<List<String>, String> book : refactorData)
            {
                for (var in : book.entrySet())
                {
                    String authors = in.getKey().stream().collect(Collectors.joining(", "));
                    String title = in.getValue();
                    writer.writeNext(new String[]{authors, title});
                }
            }

            System.out.println("Данные успешно записаны в файл " + filename);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void BRBooksMaxRateSaveToCSV(List<BookReview> data, String filename)
    {
        List<HashMap<String, Object>> refactorData = data.stream()
                .filter(BR -> BR.getRating() == 5)
                .map(BR ->
                {
                    HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put("BookAuthors", BR.getBookAuthors());
                    dataMap.put("BookTitle", BR.getBookTitle());
                    dataMap.put("URL", BR.getCoverURL());
                    return dataMap;
                })
                .collect(Collectors.toList());

        try (CSVWriter writer = new CSVWriter(new FileWriter(filename)))
        {
            String[] header = {"Авторы книги", "Название книги", "Адрес обложки"};
            writer.writeNext(header);

            for (HashMap<String, Object> book : refactorData)
            {
                List<String> authors = (List<String>) book.get("BookAuthors");
                String title = (String) book.get("BookTitle");
                URL picURL = (URL) book.get("URL");
                writer.writeNext(new String[]{String.join(", ", authors), title, picURL.toString()});
            }

            System.out.println("Данные успешно записаны в файл " + filename);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void printOnlyProsOSRevs(List<OnlineStore> data)
    {
        for (OnlineStore os : data)
        {
            List<HashMap<String, Object>> output = os.getRevs().stream()
                    .filter(OSR -> Objects.equals(OSR.getCons(), "—") || Objects.equals(OSR.getCons(), "нет") || Objects.equals(OSR.getCons(), "Нет"))
                    .map(OSR ->
                    {
                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("OSName", os.getStoreName());
                        dataMap.put("OSR", OSR.getRevText());
                        dataMap.put("OSRPros", OSR.getPros());
                        dataMap.put("OSRRate", OSR.getRating());
                        return dataMap;
                    })
                    .collect(Collectors.toList());

            for (HashMap<String, Object> dm : output)
            {
                System.out.println("Название магазина: " + dm.get("OSName").toString());
                System.out.println("Оценка: " + dm.get("OSRRate").toString());
                System.out.println("Плюсы: " + dm.get("OSRPros").toString());
                System.out.println("Отзыв: " + dm.get("OSR").toString() + "\n");
            }
        }
    }

    public void printOnlyLessThanTwoStarsOSRevs(List<OnlineStore> data)
    {
        for (OnlineStore os : data)
        {
            long cntOfRevs = os.getRevs().stream()
                    .filter(OSR -> OSR.getRating() <= 2)
                    .count();

            System.out.println("Название магазина: " + os.getStoreName());
            System.out.println("Количество отзывов с менее чем 2-мя звездами: " + cntOfRevs + "\n");
        }
    }

    public void printThreeGroupsOSRevs(List<OnlineStore> data)
    {
        for (OnlineStore os : data)
        {
            long positiveCntRevs = os.getRevs().stream()
                    .filter(OSR -> OSR.getRating() > 3)
                    .count();
            long neutralCntRevs = os.getRevs().stream()
                    .filter(OSR -> OSR.getRating() == 3)
                    .count();
            long negativeCntRevs = os.getRevs().stream()
                    .filter(OSR -> OSR.getRating() < 3)
                    .count();

            System.out.println("Название магазина: " + os.getStoreName());
            System.out.println("Количество положительных отзывов: " + positiveCntRevs);
            System.out.println("Количество нейтральных отзывов: " + neutralCntRevs);
            System.out.println("Количество отрицательных отзывов: " + negativeCntRevs + "\n");
        }
    }
}
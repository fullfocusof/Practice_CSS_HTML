package org.example.OnlineStore;

import lombok.*;
import org.example.Review.BookReview;
import org.example.Review.OSReview;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStore
{
    String storeName;
    double avgRating;
    int cntRevs;
    List<OSReview> revs;

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder(storeName + "\n" + "Отзывов: " + cntRevs + "\n" + "Средняя оценка: " + avgRating + "\n");
        for (OSReview rev : revs)
        {
            output.append(rev.toString()).append("\n\n");
        }
        return output.toString();
    }
}
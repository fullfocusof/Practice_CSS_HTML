package org.example.Review;

import lombok.*;

import java.net.URL;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review
{
    String bookTitle, bookAuthors, revAuthor, revText;
    int rating;
    URL coverURL;

    @Override
    public String toString()
    {
        return revAuthor + "\n" +
                bookAuthors + ": " + bookTitle + "\n" +
                coverURL.toString() + "\n" +
                "Оценка: " + rating + "\n" +
                revText;
    }


}
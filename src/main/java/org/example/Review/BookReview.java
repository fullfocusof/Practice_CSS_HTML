package org.example.Review;

import lombok.*;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookReview
{
    String bookTitle, revAuthor, revText;
    List<String> bookAuthors;
    int rating;
    URL coverURL;

    @Override
    public String toString()
    {
        return revAuthor + "\n"
                + ((bookAuthors.size() > 1) ? String.join(", ", bookAuthors) : bookAuthors.toString())
                + ": " + bookTitle + "\n" +
                coverURL.toString() + "\n" +
                "Оценка: " + rating + "\n" +
                revText;
    }
}
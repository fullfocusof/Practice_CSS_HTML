package org.example.Review;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OSReview
{
    String revAuthor, pros, cons, revText;
    int rating;
    
    @Override   
    public String toString()
    {
        return "\t" + revAuthor + "\n" +
                "\t" + "Оценка: " + rating + "\n" +
                "\t" + "Плюсы: " + pros + "\n" +
                "\t" + "Минусы: " + cons + "\n" +
                "\t" + revText;
    }
}

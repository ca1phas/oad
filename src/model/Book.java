package model;

import java.time.LocalDate;

import model.base.Identifiable;

public class Book implements Identifiable {
    private int id;
    private String title;
    private String author;
    private String genre;
    private LocalDate releasedDate;
    private String filename;

    public Book(int id, String title, String author, String genre, LocalDate releasedDate, String filename) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.releasedDate = releasedDate;
        this.filename = filename;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public LocalDate getReleasedDate() {
        return releasedDate;
    }

    public String getFilename() {
        return filename;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setReleasedDate(LocalDate releasedDate) {
        this.releasedDate = releasedDate;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return id + ": " + title + " by " + author
                + "in genre " + genre
                + ", released on " + releasedDate
                + ". File: " + filename;
    }

    @Override
    public String getKey() {
        return String.valueOf(id);
    }
}

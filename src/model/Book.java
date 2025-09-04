package model;

import java.time.LocalDate;

import model.base.Identifiable;

public class Book implements Identifiable {
    private int id; // Unique book ID
    private String title; // Title of the book
    private String author; // Author name
    private String genre; // Genre (e.g., Fiction, Science, History)
    private LocalDate releasedDate; // Release/publication date
    private String filename; // File name for the digital copy (txt, pdf, etc.)

    // Full constructor
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

    // For displaying book details in a readable format
    @Override
    public String toString() {
        return id + ": " + title + " by " + author
                + " in genre " + genre
                + ", released on " + releasedDate
                + ". File: " + filename;
    }

    // Used as a unique identifier key for repository operations
    @Override
    public String getKey() {
        return String.valueOf(id);
    }
}

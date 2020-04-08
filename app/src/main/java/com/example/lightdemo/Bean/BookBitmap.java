package com.example.lightdemo.Bean;

import android.graphics.drawable.Drawable;

public class BookBitmap {
    int id = 0;//书籍id
    String name = "";//书籍名
    String cover="";//书籍封面
    Drawable coverDraw = null;//封面图
    int hot = 0;//书籍热度
    int words = 0;//书籍字数

    public BookBitmap(int id) {
        this.id = id;
    }

    public Drawable getCoverDraw() {
        return coverDraw;
    }

    public void setCoverDraw(Drawable coverBit) {
        this.coverDraw = coverBit;
    }

    public Author author;

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }
}

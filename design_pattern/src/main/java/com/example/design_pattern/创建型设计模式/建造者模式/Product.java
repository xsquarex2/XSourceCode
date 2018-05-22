package com.example.design_pattern.创建型设计模式.建造者模式;

public class Product {

    private String board;
    private String display;
    private String os;

    public String getBoard() {
        return board;
    }

    public String getDisplay() {
        return display;
    }

    public String getOs() {
        return os;
    }

    private Product(Builder builder) {
        // 进行构建
        this.board = builder.board;
        this.display = builder.display;
        this.os = builder.os;
    }

    public static class Builder {
        // 建造者模式还可以设置默认值
        private String board = "default value";
        private String display = "default value";
        private String os = "default value";

        public void setBoard(String board) {
            this.board = board;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public void setOs(String os) {
            this.os = os;
        }


        public Product build() {
            return new Product(this);
        }
    }
}
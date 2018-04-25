package com.jtravan.pbs.model;

/**
 * Created by johnravan on 3/31/16.
 */
public enum Category {
    HCHE(0),
    HCLE(1),
    LCHE(2),
    LCLE(3);

    private final int categoryNum;

    Category(int categoryNum) {
        this.categoryNum = categoryNum;
    }

    public int getCategoryNum() {
        return this.categoryNum;
    }

    public static final Category getCategoryByCategoryNum(int operationNum) {

        if(operationNum == 0) {
            return HCHE;
        } else if(operationNum == 1) {
            return HCLE;
        } else if(operationNum == 2) {
            return LCHE;
        } else if(operationNum == 3) {
            return LCLE;
        } else {
            return null;
        }

    }

    public static final boolean isCategory1HigherThanCategory2(Category category1, Category category2) {
        return category1.getCategoryNum() < category2.getCategoryNum();
    }

    public static final boolean isCategory1HigherThanOrEqualCategory2(Category category1, Category category2) {
        return category1.getCategoryNum() <= category2.getCategoryNum();
    }
}

package com.gitee.randomobject.domain;

/**
 * 外键级联策略
 */
public enum ForeignKeyOption {
    /**限制*/
    RESTRICT("RESTRICT"),
    /**无操作*/
    NOACTION("NO ACTION"),
    /**设置为空*/
    SETNULL("SET NULL"),
    /**设置默认值*/
    SETDEFAULT("SET DEFAULT"),
    /**级联操作*/
    CASCADE("CASCADE");

    private String operation;

    ForeignKeyOption(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}

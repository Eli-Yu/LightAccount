package com.eli.lightaccount;

public class TypeBean {

    private String typeId;
    private String typeName;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 重载toString 以便ArrayAdapter能够正常显示，因为ListView使用ArrayAdapter<T>时，
     * 每个列表项显示的内容就是T的toString方法返回的值
     * @return 类型名
     */
    @Override
    public String toString() {
        return typeName;
    }
}

package qunar.tc.qconfig.admin.cloud.vo;

public class PropertyDiffVo {

    private String key;

    private int exist;

    private String valueFrom;

    private String valueTo;

    public PropertyDiffVo() {
    }

    public PropertyDiffVo(String key, int exist, String valueFrom, String valueTo) {
        this.key = key;
        this.exist = exist;
        this.valueFrom = valueFrom;
        this.valueTo = valueTo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public String getValueFrom() {
        return valueFrom;
    }

    public void setValueFrom(String valueFrom) {
        this.valueFrom = valueFrom;
    }

    public String getValueTo() {
        return valueTo;
    }

    public void setValueTo(String valueTo) {
        this.valueTo = valueTo;
    }

    @Override
    public String toString() {
        return "PropertyDiffVo{" +
                "key='" + key + '\'' +
                ", exist=" + exist +
                ", valueFrom='" + valueFrom + '\'' +
                ", valueTo='" + valueTo + '\'' +
                '}';
    }
}

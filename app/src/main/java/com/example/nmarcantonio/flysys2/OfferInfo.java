package com.example.nmarcantonio.flysys2;

/**
 * Created by Nicolas on 11/21/2016.
 */

public class OfferInfo {

    private String id;
    private String number;
    private String srcAir;
    private String dstAir;
    private Double price;
    private String depDate;
    private String arrDate;
    public float rating;

    public String getDepDate() {
        return depDate;
    }

    public String getArrDate() {
        return arrDate;
    }

    public OfferInfo(String id, String number, String srcAir, String dstAir, Double price, String depDate, String arrDate,float rating) {
        this.id = id;
        this.number = number;
        this.srcAir = srcAir;
        this.dstAir = dstAir;
        this.price = price;
        this.depDate = depDate;
        this.arrDate = arrDate;
        this.rating = rating;
    }


    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public float getRating(){
        return rating;
    }


    public String getDstAir() {
        return dstAir;
    }

    public void setDstAir(String dstAir) {
        this.dstAir = dstAir;
    }

    public String getSrcAir() {
        return srcAir;
    }

    public void setSrcAir(String srcAir) {
        this.srcAir = srcAir;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}


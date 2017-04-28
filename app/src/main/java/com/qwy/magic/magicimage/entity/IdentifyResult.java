package com.qwy.magic.magicimage.entity;

import org.xutils.http.annotation.HttpResponse;

import java.io.Serializable;
import java.util.List;

/**
 * Created by qwy on 17/3/29.
 * 识别结果实体
 */
public class IdentifyResult implements Serializable {

    /**
     * errorcode : 0
     * errormsg : OK
     * name : xxx
     * name_confidence_all : [50,60,53]
     * sex : xx
     * sex_confidence_all : [41]
     * nation : xx
     * nation_confidence_all : [38]
     * birth : xxxx/xx/xx
     * birth_confidence_all : [69,43,46,46,44,53,50]
     * address : xxxxxxxxx
     * address_confidence_all : [43,36,34,17,25,24,21,31,45,36,23,62,12,30,9,1,39,34,24,19,16,30,30,4]
     * id : xxxxxxxxxxxxxxxxxx
     * id_confidence_all : [52,58,59,73,54,56,61,63,50,63,60,49,72,50,62,57,63,53]
     * frontimage : xxx
     * frontimage_confidence_all : []
     * watermask_confidence_all : []
     * valid_date_confidence_all : []
     * authority_confidence_all : []
     * backimage_confidence_all : []
     * detail_errorcode : []
     * detail_errormsg : []
     */

    private int errorcode;
    private String errormsg;
    private String name;
    private String sex;
    private String nation;
    private String birth;
    private String address;
    private String id;
    private String frontimage;
    private List<Integer> name_confidence_all;
    private List<Integer> sex_confidence_all;
    private List<Integer> nation_confidence_all;
    private List<Integer> birth_confidence_all;
    private List<Integer> address_confidence_all;
    private List<Integer> id_confidence_all;
    private List<?> frontimage_confidence_all;
    private List<?> watermask_confidence_all;
    private List<?> valid_date_confidence_all;
    private List<?> authority_confidence_all;
    private List<?> backimage_confidence_all;
    private List<Integer> detail_errorcode;
    private List<String> detail_errormsg;

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrontimage() {
        return frontimage;
    }

    public void setFrontimage(String frontimage) {
        this.frontimage = frontimage;
    }

    public List<Integer> getName_confidence_all() {
        return name_confidence_all;
    }

    public void setName_confidence_all(List<Integer> name_confidence_all) {
        this.name_confidence_all = name_confidence_all;
    }

    public List<Integer> getSex_confidence_all() {
        return sex_confidence_all;
    }

    public void setSex_confidence_all(List<Integer> sex_confidence_all) {
        this.sex_confidence_all = sex_confidence_all;
    }

    public List<Integer> getNation_confidence_all() {
        return nation_confidence_all;
    }

    public void setNation_confidence_all(List<Integer> nation_confidence_all) {
        this.nation_confidence_all = nation_confidence_all;
    }

    public List<Integer> getBirth_confidence_all() {
        return birth_confidence_all;
    }

    public void setBirth_confidence_all(List<Integer> birth_confidence_all) {
        this.birth_confidence_all = birth_confidence_all;
    }

    public List<Integer> getAddress_confidence_all() {
        return address_confidence_all;
    }

    public void setAddress_confidence_all(List<Integer> address_confidence_all) {
        this.address_confidence_all = address_confidence_all;
    }

    public List<Integer> getId_confidence_all() {
        return id_confidence_all;
    }

    public void setId_confidence_all(List<Integer> id_confidence_all) {
        this.id_confidence_all = id_confidence_all;
    }

    public List<?> getFrontimage_confidence_all() {
        return frontimage_confidence_all;
    }

    public void setFrontimage_confidence_all(List<?> frontimage_confidence_all) {
        this.frontimage_confidence_all = frontimage_confidence_all;
    }

    public List<?> getWatermask_confidence_all() {
        return watermask_confidence_all;
    }

    public void setWatermask_confidence_all(List<?> watermask_confidence_all) {
        this.watermask_confidence_all = watermask_confidence_all;
    }

    public List<?> getValid_date_confidence_all() {
        return valid_date_confidence_all;
    }

    public void setValid_date_confidence_all(List<?> valid_date_confidence_all) {
        this.valid_date_confidence_all = valid_date_confidence_all;
    }

    public List<?> getAuthority_confidence_all() {
        return authority_confidence_all;
    }

    public void setAuthority_confidence_all(List<?> authority_confidence_all) {
        this.authority_confidence_all = authority_confidence_all;
    }

    public List<?> getBackimage_confidence_all() {
        return backimage_confidence_all;
    }

    public void setBackimage_confidence_all(List<?> backimage_confidence_all) {
        this.backimage_confidence_all = backimage_confidence_all;
    }

    public List<Integer> getDetail_errorcode() {
        return detail_errorcode;
    }

    public void setDetail_errorcode(List<Integer> detail_errorcode) {
        this.detail_errorcode = detail_errorcode;
    }

    public List<String> getDetail_errormsg() {
        return detail_errormsg;
    }

    public void setDetail_errormsg(List<String> detail_errormsg) {
        this.detail_errormsg = detail_errormsg;
    }
}

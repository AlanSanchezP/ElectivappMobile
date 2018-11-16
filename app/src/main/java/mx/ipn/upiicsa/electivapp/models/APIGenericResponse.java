package mx.ipn.upiicsa.electivapp.models;

public class APIGenericResponse {
    private String detail;
    private int code;

    public APIGenericResponse(String detail, int code) {
        this.detail = detail;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return this.detail;
    }
}

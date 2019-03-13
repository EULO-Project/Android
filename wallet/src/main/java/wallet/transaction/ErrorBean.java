package wallet.transaction;

/**
 * Created by LuoQiuJie on 2019/1/19.
 */

public class ErrorBean  {
    private int errno;
    private String message;


    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

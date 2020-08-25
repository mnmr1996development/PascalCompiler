public class Token {
    private String tokenType;
    private String tokenValue;

    private int colNum = 0;
    private int lineRow = 0;

    public Token(String tokenType, String tokenValue, int colNum, int lineRow){
        this.tokenType = tokenType;
        this.tokenValue = tokenValue;

        this.colNum = colNum;
        this.lineRow = lineRow;
    }

    public String toString(){
        return tokenValue;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenValue() {
        return tokenValue;
    }

}

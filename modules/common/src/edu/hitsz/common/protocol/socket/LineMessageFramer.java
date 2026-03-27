package edu.hitsz.common.protocol.socket;

public class LineMessageFramer {

    public String frame(String rawMessage) {
        return rawMessage + "\n";
    }
}

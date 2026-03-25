package edu.hitsz.application.protocol.socket;

public class LineMessageFramer {

    public String frame(String rawMessage) {
        return rawMessage + "\n";
    }
}

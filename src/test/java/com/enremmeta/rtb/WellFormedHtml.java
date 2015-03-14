package com.enremmeta.rtb;

public class WellFormedHtml {
    // TODO: implement using JTidy
    private static boolean doValidation = true;

    public static boolean validate(String htmlString) {
        if (doValidation) {
            // temporary solution
            try {
                if (!htmlString.contains("<testXmlRoot>")) {
                    htmlString = String.format("<testXmlRoot>%1$s</testXmlRoot>", htmlString);
                }
                return WellFormedXml.validate(htmlString);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        String testString = "<h1_>Admin</h1>";

        if (validate(testString)) {
            System.out.println("String is well-formed html");
        }
    }
}

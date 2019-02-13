package bitflow.distributedexperiments.objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author fschmidt
 */
public class Parameters {

    private static final String JSON_PARAM_KEY = "parameter";

    private String rawValue = "";

    private String prefix;
    private String postfix;
    private int[] numericalMin;
    private int[] numericalMax;

    public Parameters(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getParameter() {
        String parameter = prefix;
        for (int i = 0; i < numericalMax.length; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(numericalMin[i], numericalMax[i] + 1);
            if (i == numericalMax.length - 1) {
                parameter += randomNum;
            } else {
                parameter += randomNum + " ";
            }
        }
        return parameter + postfix;
    }

    public void setParameter(String defaultParam) {
        String postNumericals = "";
        int prefixCutIndex = defaultParam.indexOf("{{");
        if (prefixCutIndex == -1) {
            prefixCutIndex = defaultParam.length();
        } else {
            postNumericals = defaultParam.substring(prefixCutIndex, defaultParam.length());
        }
        prefix = defaultParam.substring(0, prefixCutIndex);

        int postfixCutIndex = defaultParam.lastIndexOf("}");
        if (postfixCutIndex == -1) {
            postfix = "";
        } else {
            postfix = defaultParam.substring(postfixCutIndex + 1, defaultParam.length());
        }

        Pattern pattern = Pattern.compile("(\\{\\{)(.*?)(\\}\\})");
        Matcher matcher = pattern.matcher(postNumericals);
        List<String> listMatches = new ArrayList<String>();
        while (matcher.find()) {
            listMatches.add(matcher.group(2));
        }
        numericalMin = new int[listMatches.size()];
        numericalMax = new int[listMatches.size()];
        int counter = 0;
        for (String s : listMatches) {
            String[] minMax = s.split(" ");
            numericalMin[counter] = Integer.parseInt(minMax[0]);
            numericalMax[counter] = Integer.parseInt(minMax[1]);
            counter++;
        }
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public JSONObject getParamsAsJSON(){
        Map<String, String> params = new HashMap<>();
        params.put(JSON_PARAM_KEY, rawValue);

        return new JSONObject(params);
    }
}

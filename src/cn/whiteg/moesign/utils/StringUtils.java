package cn.whiteg.moesign.utils;


import java.util.ArrayList;
import java.util.List;

/**
 * The class {@code StringUtils} is an utility class for string
 * processing in PicqBotX.
 * <p>
 * Class created by the HyDEV Team on 2019-03-24!
 *
 * @author HyDEV Team (https://github.com/HyDevelop)
 * @author Hykilpikonna (https://github.com/hykilpikonna)
 * @author Vanilla (https://github.com/VergeDX)
 * @since 2019-03-24 13:35
 */
public class StringUtils {
    /**
     * 移除前面的空格
     *
     * @param original 源字符串
     * @return 移除后的字符串
     */
    public static String removeStartingSpace(String original) {
        if (original.startsWith(" ")){
            char[] cars = original.toCharArray();
            for (int i = 1; i < cars.length; i++) {
                if (cars[i] != ' ') return original.substring(i);
            }
            return "";
        } else {
            return original;
        }
    }

    /**
     * 指令分解成字符串列表，并排除多余空格
     *
     * @param str 源字符串
     * @return 分解后的列表
     */
    public static List<String> commandMsgToArgs(String str) {
        if (str.isEmpty()){
            return new ArrayList<>(0);
        }
        int i = str.indexOf(' ');
        if (i == -1){
            List<String> list = new ArrayList<>(1);
            list.add(str);
            return list;
        }
        int s = 0;
        List<String> list = new ArrayList<>();
        while (true) {
            String arg = str.substring(s,i);
            if (!arg.isEmpty()) list.add(arg);
//            list.add(arg);
            s = i + 1;
            i = str.indexOf(' ',s);
            if (i == -1){
                arg = str.substring(s);
                if (!arg.isEmpty()) list.add(arg);
                break;
            }
        }
        return list;
    }

    //将列表组合成一个字符串
    public static String join(List<String> list,String arg) {
        if (list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(arg);
        }
        sb.delete(sb.length() - arg.length(),sb.length());
        return sb.toString();
    }

    //将列表组合成一个字符串
    public static String join(String[] array,String arg) {
        if (array.length <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s).append(arg);
        }
        sb.delete(sb.length() - arg.length(),sb.length());
        return sb.toString();
    }
}

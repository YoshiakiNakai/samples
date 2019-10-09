/**
 * Created by Nakai Yoshiaki on 2017/12/10.
 */

public class AbnormalState {

    Character chara;

    String ID = "";
    String changeCond = "";      // 変化条件
    String name = "";           //名前
    String img = "";            //画像
    String explain = "";        //説明文
    String sufText = "";        //使用時メッセージ 原文
    String effText = "";        //効果メッセージ 原文
    String recText = "";
    String sufDispText = "";       //表示用テキスト
    String effDispText = "";       //表示用テキスト
    String recDispText = "";
    String recoverCond = "";    // 回復条件 原文
    String effCond = "";         // 発動条件 原文

    String timing = "";       //計算タイミング

    //対象リソース
    String calcSign = "";        //効果の演算子
    String objRes = "";     //対象リソース 原文
    String objValue = "";   //その効果量  原文
    String objMagnify = ""; //倍率    原文


    int time = 0;   // 状態の時間
    int sp = 1;    // 状態の時間増加度  // キャラの maxSp

    int charaActCount = 0;  // キャラの行動回数
    int abnoActCount = 0;   // 状態異常発動回数

    //フラグ操作
    //String putGFlag = "";

    // trueなら回復
    boolean checkRecover() {
        String[] fileflags = recoverCond.split("&");
        for (int i = 0; i < fileflags.length; i++) {
            if(checkAbnoCond(fileflags[i]) == false) { // [0]が空文字でもtrue
                return false;
            }
        }
        return true;
    }

    boolean checkAbnoAct(){
        String[] fileflags = effCond.split("&");
        for (int i = 0; i < fileflags.length; i++) {
            if(checkAbnoCond(fileflags[i]) == false) { // [0]が空文字でもtrue
                return false;
            }
        }
        return true;
    }


    // 状態異常効果を発生させるか確認---------------------------------------------------------------
    // 被弾時
    boolean checkAbnoWhenAttacked() {
        if (timing.equals("被弾") == false) return false;
        if (checkAbnoAct() == false) return false;
        calcAbnoEffect();
        abnoActCount++;
        return true;
    }
    // 行動時
    boolean checkAbnoWhenAct(){
        charaActCount++;
        if(timing.equals("使用") == false) return false;
        if (checkAbnoAct() == false) return false;
        calcAbnoEffect();
        abnoActCount++;
        return true;
    }

    // 時間カウント時  //発動、回復チェックと、速さカウントへの影響
    boolean checkAbnoWhenTimeCount(){
        time += sp; // 状態タイムカウント
        if(timing.equals("発動") ==false && timing.equals("速さ") == false) return false;
        if (checkAbnoAct() == false) return false;

        if(timing.equals("発動")){
            time = 0;   //発動型なら発動時に time=0にする
        }   //速さは timeをカウントし続ける
        calcAbnoEffect();
        abnoActCount++;
        return true;
    }


    // 状態異常効果の計算 -----------------------------------------------------------------------------
    void calcAbnoEffect(){

        //効果対象
        String[] signs = calcSign.split("&");    // 演算子
        String[] objres = objRes.split("&");      //どのリソースに効果を与えるか
        String[] objvalue = objValue.split("&");    //その効果量
        String[] magnify = objMagnify.split("&");     //倍率
        //対象リソースごとに処理
        for(int i=0; i<objres.length; i++){
            String sign = signs[i];
            String resName = objres[i];
            String resValue = objvalue[i];
            String resMagnify = magnify[i];

            float power = 0;
            power = parseValueText2power(resValue, resMagnify, chara);  //威力を求める
            switch(resName){
                //効果対象ごとに分岐
                case("charaTime"):
                    chara.actTime = (int)textSignCalc(sign, (float)chara.actTime, power);
                    break;
                case("tmpInjuredMg"):
                    // tmpInjuredMg は float
                    chara.tmpInjuredMg = textSignCalc(sign, chara.tmpInjuredMg, power);
                    break;
                case("nHP"):
                    chara.nHP = (int)textSignCalc(sign, (float)chara.nHP, power);
                    break;
                case("nMP"):
                    chara.nMP = (int)textSignCalc(sign, (float)chara.nMP, power);
                    break;
                case("nAtk"):
                    chara.nAtk = (int)textSignCalc(sign, (float)chara.nAtk, power);
                    break;
                case("nDef"):
                    chara.nDef = (int)textSignCalc(sign, (float)chara.nDef, power);
                    break;
                case("nSp"):
                    chara.nSp = (int)textSignCalc(sign, (float)chara.nSp, power);
                    break;
                case("tmpAtk"):
                    chara.tmpAtk = (int)textSignCalc(sign, (float)chara.tmpAtk, power);
                    break;
                case("tmpDef"):
                    chara.tmpDef = (int)textSignCalc(sign, (float)chara.tmpDef, power);
                    break;
                case("tmpSp"):
                    chara.tmpSp = (int)textSignCalc(sign, (float)chara.tmpSp, power);
                    break;
            }
        }
    }

    // 状態異常によるステータス値を知りたいときに使用 tmp値のみ計算
    void calcTmpEffect(){
        //効果対象
        String[] signs = calcSign.split("&");    // 演算子
        String[] objres = objRes.split("&");      //どのリソースに効果を与えるか
        String[] objvalue = objValue.split("&");    //その効果量
        String[] magnify = objMagnify.split("&");     //倍率
        //対象リソースごとに処理
        for(int i=0; i<objres.length; i++){
            String sign = signs[i];
            String resName = objres[i];
            String resValue = objvalue[i];
            String resMagnify = magnify[i];
            float power = 0;
            power = parseValueText2power(resValue, resMagnify, chara);  //威力を求める
            switch(resName){
                //効果対象ごとに分岐
                case("tmpInjuredMg"):
                    // tmpInjuredMg は float
                    chara.tmpInjuredMg = textSignCalc(sign, chara.tmpInjuredMg, power);
                    break;
                case("tmpAtk"):
                    chara.tmpAtk = (int)textSignCalc(sign, (float)chara.tmpAtk, power);
                    break;
                case("tmpDef"):
                    chara.tmpDef = (int)textSignCalc(sign, (float)chara.tmpDef, power);
                    break;
                case("tmpSp"):
                    chara.tmpSp = (int)textSignCalc(sign, (float)chara.tmpSp, power);
                    break;
            }
        }
    }


    // str型演算子で計算、少数の掛け算のためにfloat
    float textSignCalc(String sign, float obj, float value){
        float result = 0;
        switch(sign){
            case("+"):
                result = obj + value;
                break;
            case("-"):
                result = obj - value;
                break;
            case("*"):
                result = obj * value;
                break;
            case("/"):
                result = obj / value;
                break;
            case("="):
                result = value;
                break;
        }
        return result;
    }


    // -----------------------------------------------------------------------------------------
    //効果量の計算を行う     // 引数 (威力、倍率、参照ステータス)
    float parseValueText2power(String valueText, String magnifyText, Character chara){
        float value = 0;

        // valueText // nAtk=+=maxMP
        String[] basePowers = valueText.split("\\+");
        for(int i=0; i<basePowers.length; i++) {
            // 基本効果量の取得
            switch (basePowers[i]) {
                case ("HP"):
                    value += chara.HP;
                    break;
                case ("nHP"):
                    value += chara.nHP;
                    break;
                case ("maxHP"):
                    value += chara.maxHP;
                    break;
                case ("MP"):
                    value += chara.MP;
                    break;
                case ("nMP"):
                    value += chara.nMP;
                    break;
                case ("maxMP"):
                    value += chara.maxMP;
                    break;
                case ("nAtk"):
                    value += chara.nAtk;
                    break;
                case ("nDef"):
                    value += chara.nDef;
                    break;
                case ("nSp"):
                    value += chara.nSp;
                    break;
                case ("tmpAtk"):
                    value += chara.tmpAtk;
                    break;
                case ("tmpDef"):
                    value += chara.tmpDef;
                    break;
                case ("tmpSp"):
                    value += chara.tmpSp;
                    break;

                default:
                    value += str2int(valueText);
                    break;
            }
        }

        //倍率の処理
        if(magnifyText != null && magnifyText.equals("") == false) {    //nullでも空文字でもないときのみ処理
            value *= parseMagnifyText(magnifyText);
        }
        return value;
    }

    //  倍率取得
    float parseMagnifyText(String magnifyText){
        float value = 1;
        switch(magnifyText){
            case(""):   //何もしない
                break;
            default:    //数字のとき
                value = str2float(magnifyText);
                break;
        }
        return value;
    }




    // 満たしたらtrue
    boolean checkAbnoCond(String fileCondText){
        if(fileCondText.length() == 0) return true;  // 空っぽならtrue
        String[] flagCond = fileCondText.split("=");
        switch(flagCond[0]){
            case("time"):   // time=>=50
                // 条件比較
                if (flagCond[1].equals(">")) {       // ">"の判定
                    if ( time > Integer.valueOf(flagCond[2])) return true;
                    else return false;
                } else if (flagCond[1].equals("<")) { // "<"の判定
                    if ( time < Integer.valueOf(flagCond[2])) return true;
                    else return false;
                }   // "="の判定
                else if ( time == Integer.valueOf(flagCond[1])) return true;
                else return false;
            case("act"):
                // 条件比較
                if (flagCond[1].equals(">")) {       // ">"の判定
                    if ( abnoActCount > Integer.valueOf(flagCond[2])) return true;
                    else return false;
                } else if (flagCond[1].equals("<")) { // "<"の判定
                    if ( abnoActCount < Integer.valueOf(flagCond[2])) return true;
                    else return false;
                }   // "="の判定
                else if ( abnoActCount == Integer.valueOf(flagCond[1])) return true;
                else return false;

            case("charaTime"):
                // 条件比較
                if (flagCond[1].equals(">")) {       // ">"の判定
                    if ( chara.actTime > Integer.valueOf(flagCond[2])) return true;
                    else return false;
                } else if (flagCond[1].equals("<")) { // "<"の判定
                    if ( chara.actTime < Integer.valueOf(flagCond[2])) return true;
                    else return false;
                }   // "="の判定
                else if ( chara.actTime == Integer.valueOf(flagCond[1])) return true;
                else return false;

            case("charaAct"):
                // 条件比較
                if (flagCond[1].equals(">")) {       // ">"の判定
                    if ( charaActCount > Integer.valueOf(flagCond[2])) return true;
                    else return false;
                } else if (flagCond[1].equals("<")) { // "<"の判定
                    if ( charaActCount < Integer.valueOf(flagCond[2])) return true;
                    else return false;
                }   // "="の判定
                else if ( charaActCount == Integer.valueOf(flagCond[1])) return true;
                else return false;

        }//end switch()
        return false;
    }



    // brを改行に変換する
    String raw2message(String raw) {
        String message = "";
        String[] r = raw.split("&");
        for (int i = 0; i < r.length; i++) {
            switch (r[i]) {
                case ("br"):
                    message += "\n";
                    break;
                default:
                    message += r[i];
                    break;
            }
        }
        return message;
    }

    // エラーチェックしつつ Stringからintに変換する
    int str2int(String str){
        if(str.length() == 0) return 0;
        return Integer.valueOf(str);
    }

    float str2float(String str){
        if(str.length() == 0) return 0;
        return Float.valueOf(str);
    }

}

    /*
    // 威力
    //int pownTime = 0;
    //int pownHP = 0;
    //int pownMP = 0;
    //int pownAtk = 0;
    //int pownDef = 0;
    //int pownSp = 0;

    void calcAbnoPower(){

        // 実際の効果量の算出処理
        //効果対象
        String[] objres = objRes.split("&");      //どのリソースに効果を与えるか
        String[] objvalue = objValue.split("&");    //その効果量
        String[] magnify = objMagnify.split("&");     //倍率
        //対象リソースごとに処理
        for(int i=0; i<objres.length; i++){
            String resName = objres[i];
            String resValue = objvalue[i];
            String resMagnify = magnify[i];
            switch(resName){
                //効果対象ごとに分岐
                case("time"):
                    // 威力を求める
                    pownTime = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("nHP"):
                    pownHP = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("nMP"):
                    pownMP = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("nAtk"):
                    pownAtk = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("nDef"):
                    pownDef = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("nSp"):
                    pownSp = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
            }
        }

        abno.sufDispText = ;
        abno.effDispText = ;

    }*/

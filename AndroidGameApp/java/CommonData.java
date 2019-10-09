package amai.box_world1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Nakai Yoshiaki on 2017/12/01.
 */

// ゲーム内共通データ
public class CommonData {

    //State遷移用の変数       // GameStateに入れた方がいいかも
    String nextState = "";
    String extFileNames = "";

    // 固定外部データ
    ArrayList<ArrayList<String>> constFileData = new ArrayList<>(); //アイテム・技能、装備、敵、スキル習得条件、状態異常

    // キャラメンバー
    ArrayList<Character> charaMem = new ArrayList<>();

    //金、持ち物
    int money=0;
    ArrayList<Skill> bel = new ArrayList<>();

    //global flag
    Map<String, String> strGFlag = new HashMap<>();
    Map<String, Integer> intGFlag = new HashMap<>();
    String tmpLocal = "";   // localStateの受け渡しに使う

    int story=0;
    String paragraph = "";
    String page = "";


    // 進行度に合わせて数値をいじる
    //int storyRevisionValue(){
    /*int srv(){
            int value=0;
        value = story/10;
        return value;
    }*/

    // =====================constFileDataの処理================================================================================

    // アイテム・技能ファイルの処理^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // スキル使用 --------------------
    void doSkill( Character sbj, Character obj){  // 関数の引数は参照を受け取ってます
        if(sbj.checkSklRes() == false){
            // 使用リソースが足りないとき
            sbj.btlsk.useDispText = sbj.name + "は行動に失敗した。";
            sbj.actTime -= sbj.btlsk.needTime;
        }
        // 状態異常処理とそのメッセージ、計算用tmp能力値生成
        String actAbnoMess = "";                  //<<<<<<<<<<<状態異常処理<<<<<<<<<<<<<<<
        String injAbnoMess = "";
        actAbnoMess += sbj.setTmpStatus("Act");
        if(sbj.btlsk.category.equals("ダメージ")) {
            injAbnoMess += obj.setTmpStatus("Injured");
        }else{
            injAbnoMess += obj.setTmpStatus("kaihuku");
        }

        //威力の計算
        sbj.btlsk = setSkillPower(sbj); // pow値の計算
        //抵抗値の計算
        sbj.btlsk.resistValue = parseValueText2power(sbj.btlsk.resistText, null, obj);  // objの値をsbj.resistValueに入れる
        //対象者への効果量の計算
        sbj.btlsk.effTime = sbj.btlsk.pownTime - sbj.btlsk.resistValue;
        sbj.btlsk.effmaxHP = sbj.btlsk.powmaxHP - sbj.btlsk.resistValue;
        sbj.btlsk.effnHP = sbj.btlsk.pownHP - sbj.btlsk.resistValue;
        sbj.btlsk.effnMP = sbj.btlsk.pownMP - sbj.btlsk.resistValue;
        sbj.btlsk.effnAtk = sbj.btlsk.pownAtk - sbj.btlsk.resistValue;
        sbj.btlsk.effnDef = sbj.btlsk.pownDef - sbj.btlsk.resistValue;
        sbj.btlsk.effnSp = sbj.btlsk.pownSp - sbj.btlsk.resistValue;
        sbj.btlsk = checkSkillEff(sbj.btlsk);               //スキル効果が負になっていないかチェック

        // 処理
        sbj.actTime -= sbj.btlsk.needTime;
        sbj.actTime -= sbj.btlsk.spendTime;
        sbj.maxHP -= sbj.btlsk.spendmaxHP;
        sbj.nHP -= sbj.btlsk.spendnHP;
        sbj.nMP -= sbj.btlsk.spendnMP;
        sbj.nAtk -= sbj.btlsk.spendnAtk;
        sbj.nDef -= sbj.btlsk.spendnDef;
        sbj.nSp -= sbj.btlsk.spendnSp;

        switch (sbj.btlsk.category) {     //スキルカテゴリによって処理を分岐
            case ("回復"):
                obj.maxHP += sbj.btlsk.effmaxHP;
                // 最大値を超えている場合の処理
                if (obj.nHP + sbj.btlsk.effnHP > obj.maxHP) {
                    sbj.btlsk.effnHP = obj.maxHP - obj.nHP;
                }
                if (obj.nMP + sbj.btlsk.effnMP > obj.maxMP) {
                    sbj.btlsk.effnMP = obj.maxMP - obj.nMP;
                }
                if (obj.nAtk + sbj.btlsk.effnAtk > obj.maxAtk) {
                    sbj.btlsk.effnAtk = obj.maxAtk - obj.nAtk;
                }
                if (obj.nDef + sbj.btlsk.effnDef > obj.maxDef) {
                    sbj.btlsk.effnDef = obj.maxDef - obj.nDef;
                }
                if (obj.nSp + sbj.btlsk.effnSp > obj.maxSp) {
                    sbj.btlsk.effnSp = obj.maxSp - obj.nSp;
                }
                // 処理
                obj.actTime += sbj.btlsk.effTime;
                obj.nHP += sbj.btlsk.effnHP;
                obj.nMP += sbj.btlsk.effnMP;
                obj.nAtk += sbj.btlsk.effnAtk;
                obj.nDef += sbj.btlsk.effnDef;
                obj.nSp += sbj.btlsk.effnSp;
                break;
            case ("ダメージ"):
                // tmpInjuredMg補正をかける
                sbj.btlsk.effTime *= obj.tmpInjuredMg;
                sbj.btlsk.effmaxHP *= obj.tmpInjuredMg;
                sbj.btlsk.effnHP *= obj.tmpInjuredMg;
                sbj.btlsk.effnMP *= obj.tmpInjuredMg;
                sbj.btlsk.effnAtk *= obj.tmpInjuredMg;
                sbj.btlsk.effnDef *= obj.tmpInjuredMg;
                sbj.btlsk.effnSp *= obj.tmpInjuredMg;
                // ダメージを与える
                obj.actTime -= sbj.btlsk.effTime;
                obj.maxHP -= sbj.btlsk.effmaxHP;
                obj.nHP -= sbj.btlsk.effnHP;
                obj.nMP -= sbj.btlsk.effnMP;
                obj.nAtk -= sbj.btlsk.effnAtk;
                obj.nDef -= sbj.btlsk.effnDef;
                obj.nSp -= sbj.btlsk.effnSp;

                break;
        }
        // ステータスが最大値を超えていないかチェック
        //sbj.checkStatus();
        //obj.checkStatus();

        //表示文作成
        sbj.btlsk.useDispText = raw2btlskUseText(sbj, obj);    //使用文
        sbj.btlsk.effDispText = raw2btlskEffText(sbj, obj);    //効果文
        // 状態異常
        sbj.btlsk.useDispText += actAbnoMess + injAbnoMess;
        sbj.btlsk.effDispText += setAbnoGetMess(sbj.btlsk.abnoText, obj);   // 状態付与と罹患メッセージ取得

        updateGlobalFlag(sbj.btlsk.putGFlag);    //スキルによるグローバルフラグの更新

        return;
    }


    // スキル基本情報 ------------------------------------------------------------------------------
    Skill setSkill(String skillID){
        Skill ski = new Skill();
        String[] info = getSkillInfo(skillID);

        ski.ID = info[0];
        //ski.learnCond = info[1];    //スキル習得条件
        ski.name = info[2]; //名前
        ski.img = info[3];  //画像
        ski.explain = raw2message(info[4]);  //説明文

        ski.price = str2int(info[5]);   //値段
        //ski.sellPrice = str2int();   //売却価格
        ski.sort = info[6];         // 種類
        ski.commandText = info[7];  //ボタンテキスト
        //if(ski.commandText.length() == 0) ski.commandText  = ski.name;
        ski.waitText = info[8];
        ski.useText = info[9];      //使用メッセージ   表示用処理前
        ski.effText = info[10];      //対象メッセージ   表示用処理前
        ski.needTime = str2int(info[11]);    //発動時間

        //使用リソース
        String[] useRes = info[12].split("&");      //どのリソースを消費するか
        String[] useValue = info[13].split("&");    //その消費量
        for(int i=0; i<useRes.length; i++){
            String resName = useRes[i];
            String resValue = useValue[i];
            switch(resName){
                // 消費リソースごとに分岐して処理
                case("time"):
                    ski.spendTime = str2int(resValue);  // 反動時間
                    break;
                case("maxHP"):
                    ski.spendmaxHP = str2int(resValue);    // 消費nHP
                    break;
                case("nHP"):
                    ski.spendnHP = str2int(resValue);    // 消費nHP
                    break;
                case("nMP"):
                    ski.spendnMP = str2int(resValue);    //消費nMP
                    break;
                case("nAtk"):
                    ski.spendnAtk = str2int(resValue);
                    break;
                case("nDef"):
                    ski.spendnDef = str2int(resValue);
                    break;
                case("nSp"):
                    ski.spendnSp = str2int(resValue);
                    break;
            }
        }

        //ski.waitEff = info[]; //待機中効果
        //ski.waitPow = info[]; // 効果量
        String[] waitEff = info[14].split("&");      //どのリソースを消費するか
        String[] waitPow = info[15].split("&");    //その消費量
        ski.waitAbno = info[16];//付与状態
        for(int i=0; i<waitEff.length; i++){
            String resName = waitEff[i];
            String resValue = waitPow[i];
            switch(resName){
                // リソースごとに分岐して処理
                case("wAtk"):
                    //ski.wAtk = str2int(resValue);
                    break;
                case("wDef"):
                    ski.wDef = str2int(resValue);
                    break;
            }
        }

        ski.category = info[17];    //スキル分類
        ski.objRes = info[18];      //対象リソース 原文
        ski.objValue = info[19];    //その効果量  原文
        ski.objMagnify = info[20];     //倍率    原文
        ski.resistText = info[21];      //抵抗値 原文    // resistValueは calcEffect()の流用で計算
        ski.abnoText = info[22];
        ski.putGFlag = info[23];

        ski.explainEff = makeExplainEffMess(ski);

        return ski;
    }

    // スキル基本情報文章の作成
    String makeExplainEffMess(Skill ski){

        int power = 0;  //ダメージ威力
        String addPower = "";   //追加威力
        //効果対象
        String[] objRes = ski.objRes.split("&");      //どのリソースに効果を与えるか
        String[] objValue = ski.objValue.split("&");    //その効果量
        String[] magnify = ski.objMagnify.split("&");     //倍率
        //対象リソースごとに処理
        for(int i=0; i<objRes.length; i++) {
            String resName = objRes[i];
            String resValue = objValue[i];  // nAtk+nMP
            //String resMagnify = magnify[i];   // 倍率は換算しない
            switch (resName) {
                //対象リソースごとに分岐
                case ("nHP"):    // ダメージへの効果量のみ取得する
                    String[] basePowers = resValue.split("\\+");
                    for (int k = 0; k < basePowers.length; k++) {
                        // 基本効果量の取得
                        switch (basePowers[k]) {
                            case ("maxHP"): //キャラステータス依存
                            case ("maxMP"):
                            case ("nHP"):
                            case ("nMP"):
                            case ("nAtk"):
                            case ("nDef"):
                            case ("nSp"):
                            case ("tmpAtk"):
                            case ("tmpDef"):
                            case ("tmpSp"):
                                addPower += "+";
                                break;
                            default:    // 数値
                                power += str2int(basePowers[k]);
                                break;
                        }
                    }
            }
            break;
        }

        // 威力、防御、ウェイト、反動、特殊効果
        String mess = "";
        mess += "威力 " + power;  mess += addPower;
        mess += "  防御 " + ski.wDef;
        mess += "  ウェイト " + ski.needTime;
        // 残りはスキル説明欄で説明する
        //mess += "  反動 " + ski.spendTime;
        //mess += "  特殊 ";
        return mess;
    }



    // スキル効果をチェックする
    Skill checkSkillEff(Skill ski) {
        if(ski.effmaxHP < 0) ski.effmaxHP = 0;
        if (ski.effnHP < 0) ski.effnHP = 0;
        if (ski.effnMP < 0) ski.effnMP = 0;
        if (ski.effTime < 0) ski.effTime = 0;
        if(ski.effnAtk < 0) ski.effnAtk = 0;
        if(ski.effnDef < 0) ski.effnDef = 0;
        if(ski.effnSp < 0) ski.effnSp = 0;
        return ski;
    }

    //効果対象とその威力を得る ------------------------------------------------------------------------
    Skill setSkillPower(Character chara){
        // 待機メッセージの作成
        chara.btlsk.waitDispText = raw2btlskWaitText(chara);

        //効果対象
        String[] objRes = chara.btlsk.objRes.split("&");      //どのリソースに効果を与えるか
        String[] objValue = chara.btlsk.objValue.split("&");    //その効果量
        String[] magnify = chara.btlsk.objMagnify.split("&");     //倍率
        //対象リソースごとに処理
        for(int i=0; i<objRes.length; i++){
            String resName = objRes[i];
            String resValue = objValue[i];  // nAtk+nMP
            String resMagnify = magnify[i];
            /*
            try{
                resMagnify = magnify[i];
            }catch (java.lang.ArrayIndexOutOfBoundsException e){
                resMagnify = "";
            }*/
            switch(resName){
                //対象リソースごとに分岐
                case("time"):
                    chara.btlsk.pownTime = parseValueText2power(resValue, resMagnify, chara);  //
                    break;
                case("maxHP"):
                    chara.btlsk.powmaxHP = parseValueText2power(resValue, resMagnify, chara);
                    break;
                case("nHP"):
                    chara.btlsk.pownHP = parseValueText2power(resValue, resMagnify, chara);
                    break;
                case("nMP"):
                    chara.btlsk.pownMP = parseValueText2power(resValue, resMagnify, chara);
                    break;
                case("nAtk"):
                    chara.btlsk.pownAtk = parseValueText2power(resValue, resMagnify, chara);
                    break;
                case("nDef"):
                    chara.btlsk.pownDef = parseValueText2power(resValue, resMagnify, chara);
                    break;
                case("nSp"):
                    chara.btlsk.pownSp = parseValueText2power(resValue, resMagnify, chara);
                    break;
            }
        }

        return chara.btlsk;
    }

    // -----------------------------------------------------------------------------------------
    //効果量の計算を行う     // 引数 (威力、倍率、参照ステータス)
    int parseValueText2power(String valueText, String magnifyText, Character chara){
        int value = 0;

        // valueText // nAtk+maxMP
        String[] basePowers = valueText.split("\\+");
        for(int i=0; i<basePowers.length; i++) {
            // 基本効果量の取得
            switch (basePowers[i]) {
            /*
            case("HP"):
                value += chara.HP;
                break;*/
            /*case("MP"):
                value += chara.MP;
                break;*/
                case ("maxHP"):
                    value += chara.maxHP;
                    break;
                case ("maxMP"):
                    value += chara.maxMP;
                    break;
                case ("nHP"):
                    value += chara.nHP;
                    break;
                case ("nMP"):
                    value += chara.nMP;
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
                    value += str2int(basePowers[i]);
                    break;
            }
        }

        //倍率の処理
        if(magnifyText != null && magnifyText.equals("") == false) {    //nullでも空文字でもないときのみ処理
            value *= parseMagnifyText(magnifyText); // int * float
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

    float str2float(String str){
        if(str.length() == 0) return 0;
        return Float.valueOf(str);
    }

    // 予約語を変換して、使用メッセージ、対象メッセージを作る
    String raw2btlskWaitText(Character sbj) {     // name, sbj, obj, nHP, ...
        String waitMessage = "";
        //使用者メッセージの作成
        String[] waitText = sbj.btlsk.waitText.split("&");
        for (int i = 0; i < waitText.length; i++) {
            switch (waitText[i]) {
                case ("name"):
                    waitMessage += sbj.btlsk.name;
                    break;
                case ("sbj"):
                    waitMessage += sbj.name;
                    break;
                default:
                    waitMessage += waitText[i];
                    break;
            }
        }
        return waitMessage;
    }

    // 予約語を変換して、使用メッセージ、対象メッセージを作る
    String raw2btlskUseText(Character sbj, Character obj) {     // name, sbj, obj, nHP, ...
        String useMessage = "";
        //使用者メッセージの作成
        String[] useTxt = sbj.btlsk.useText.split("&");
        for (int i = 0; i < useTxt.length; i++) {
            switch (useTxt[i]) {
                case ("name"):
                    useMessage += sbj.btlsk.name;
                    break;
                case ("sbj"):
                    useMessage += sbj.name;
                    break;
                case ("obj"):
                    useMessage += obj.name;
                    break;
                case("br"):
                    useMessage += "\n";
                    break;
                /*
                case ("nHP"):
                    useMessage += sbj.btlsk.spendnHP;
                    break;
                case ("nMP"):
                    useMessage += sbj.btlsk.spendnMP;
                    break;*/
                default:
                    useMessage += useTxt[i];
                    break;
            }
        }
        //useMessage += "\n";
        return useMessage;
    }

    // 予約語を変換して、使用メッセージ、対象メッセージを作る
    String raw2btlskEffText(Character sbj, Character obj){     // name, sbj, obj, nHP, ...
        //対象者メッセージの作成
        String effMessage = "";
        String[] effTxt = sbj.btlsk.effText.split("&");
        for(int i=0; i<effTxt.length; i++){
            switch( effTxt[i]){
                case("br"):
                    effMessage += "\n";
                    break;
                case("name"):
                    effMessage += sbj.btlsk.name;
                    break;
                case("sbj"):
                    effMessage += sbj.name;
                    break;
                case("obj"):
                    effMessage += obj.name;
                    break;
                case("effnHP"):// 効果量を変換しないといけないので専用の関数が必要
                    effMessage += sbj.btlsk.effnHP;
                    break;
                case("effnMP"):
                    effMessage += sbj.btlsk.effnMP;
                    break;
                default:
                    effMessage += effTxt[i];
                    break;
            }
        }
        //effMessage += "\n";

        return effMessage;
    }


    // アイテム・技能　ここまで^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // ============================== 状態異常ファイル処理 ============================================================
    //
    String setAbnoGetMess(String sklAbnoText, Character chara){
        if(sklAbnoText.length() == 0) return "";

        String mess = "";
        String[] abnoIDs = sklAbnoText.split("&");
        for(int i=0; i<abnoIDs.length; i++) {
            AbnormalState abno = getAbno(abnoIDs[i], chara);
            chara.btlstates.add(abno);
            mess += abno.sufDispText;
        }
        return mess;
    }

    AbnormalState getAbno(String abnoID, Character chara) {
        AbnormalState abno = new AbnormalState();
        String[] fileInfo = getAbnoInfo(abnoID);

        abno.chara = chara;
        abno.sp = chara.maxSp;    // <<< キャラの maxSp

        abno.ID = fileInfo[0];
        abno.changeCond = fileInfo[1];      // 変化条件
        abno.name = fileInfo[2];           //名前
        abno.img = fileInfo[3];            //画像
        abno.explain = fileInfo[4];        //説明文
        abno.sufText = fileInfo[5];        //使用時メッセージ 原文
        abno.effText = fileInfo[6];        //効果メッセージ 原文
        abno.recText = fileInfo[7];

        abno.timing = fileInfo[8];       //状態分類

        abno.recoverCond = fileInfo[9];    // 回復条件 原文
        abno.effCond = fileInfo[10];         // 発動条件 原文


        //対象リソース
        abno.calcSign = fileInfo[11];        //効果分類
        abno.objRes = fileInfo[12];     //対象リソース 原文
        abno.objValue = fileInfo[13];   //その効果量  原文
        abno.objMagnify = fileInfo[14]; //倍率    原文

        //フラグ操作
        //abno.putGFlag = fileInfo[14];


        abno.sufDispText = raw2abnoMess(abno.sufText, chara);
        abno.effDispText = raw2abnoMess(abno.effText, chara);
        abno.recDispText = raw2abnoMess(abno.recText, chara);


        return abno;
    }

    // 予約語を変換する
    String raw2abnoMess (String rawText, Character chara){     // name, sbj, obj, nHP, ...
        //対象者メッセージの作成
        String abnoMessage = "";
        String[] abnoText = rawText.split("&");
        for (int i = 0; i < abnoText.length; i++) {
            switch (abnoText[i]) {
                case ("br"):
                    abnoMessage += "\n";
                    break;
                case ("sbj"):
                    abnoMessage += chara.name;
                    break;
                default:
                    abnoMessage += abnoText[i];
                    break;
            }
        }
        //effMessage += "\n";

        return abnoMessage;
    }


        // ============================== Characterクラス処理 ============================================================

    // キャラIDからキャラを入手
    Character getChara(String charaID){
        for(int i=0; i<charaMem.size(); i++) {
            Character chara = charaMem.get(i);
            if (chara.ID.equals(charaID)) {   //IDチェック
                return chara;
            }
        }
        return null;
    }

    // 敵のセット enemyファイルの処理----------------------------
    Character setEnemy(String enemyID) {
        Character enemy = new Character();
        // 敵名からデータを取得
        String[] enemyInfo = getEnemyInfo(enemyID);
        // 敵をセット
        enemy.ID = enemyInfo[0];
        enemy.name = enemyInfo[1];
        enemy.img = enemyInfo[2];
        enemy.btlBgImg = enemyInfo[3];
        //enemy.explain = raw2message( enemyInfo[]);
        //enemy.putGFlag = enemyInfo[];

        String[] bgmInfo = enemyInfo[6].split("=");
        if(bgmInfo.length == 2) {
            enemy.btlBgm = bgmInfo[0];
            enemy.bgmRePos = str2int(bgmInfo[1]);
        }

        enemy.maxHP = str2int(enemyInfo[7]);
        enemy.maxMP = str2int(enemyInfo[8]);
        enemy.maxAtk = str2int(enemyInfo[9]);
        enemy.maxDef = str2int(enemyInfo[10]);
        enemy.maxSp = str2int(enemyInfo[11]);

        enemy.experience = str2int(enemyInfo[12]);
        enemy.escapeDifficult = str2int(enemyInfo[13]);

        // 敵の使用スキルと使用条件
        //ランダム技
        String[] rndSkis = enemyInfo[14].split("&");//ファイア=1&エーテル=0&通常攻撃=1
        for(int i=0; i<rndSkis.length; i++){
            String[] skiIdPri = rndSkis[i].split("=");
            if(skiIdPri.length != 2) continue; //フォーマットが正しいか確認
            enemy.skills.add(setSkill(skiIdPri[0]));
            enemy.btlskPriority.add(str2int(skiIdPri[1]));
        }
        //指定ターン技
        String[] turnSkis = enemyInfo[15].split("&");//消えゆく世界=1&終わらせる魔法=50
        for(int i=0; i<turnSkis.length; i++){
            String[] skiIdTurn = turnSkis[i].split("=");
            if(skiIdTurn.length != 2) continue; //フォーマットが正しいか確認
            enemy.turnSkills.add(setSkill(skiIdTurn[0]));
            enemy.btlskTurn.add(str2int(skiIdTurn[1]));
        }
        //指定ターン毎の技
        String[] perTurnSkis = enemyInfo[16].split("&");//プラウドオブハーデス=7&死神の鎌=4
        for(int i=0; i<perTurnSkis.length; i++){
            String[] skiIDperTurn = perTurnSkis[i].split("=");
            if(skiIDperTurn.length != 2) continue; //フォーマットが正しいか確認
            enemy.perTurnSkills.add(setSkill(skiIDperTurn[0]));
            enemy.btlskPerTurn.add(str2int(skiIDperTurn[1]));
        }
        //相手依存の技
        String[] dependSkis = enemyInfo[17].split("&");
        for(int i=0; i<dependSkis.length; i++){
            if(dependSkis[i].length() == 0) continue;
            enemy.dependSkills.add(setSkill(dependSkis[i]));
            enemy.btlskDependAtk.add(enemyInfo[18].split("&")[i]);
            enemy.btlskDependDef.add(enemyInfo[19].split("&")[i]);
            enemy.btlskDependWait.add(enemyInfo[20].split("&")[i]);
            enemy.btlskDependSpecial.add(enemyInfo[21].split("&")[i]);
            enemy.btlskDependMy.add(enemyInfo[22].split("&")[i]);
        }

        enemy.nHP = enemy.maxHP;
        enemy.nMP = enemy.maxMP;
        enemy.nAtk = enemy.maxAtk;
        enemy.nDef = enemy.maxDef;
        enemy.nSp = enemy.maxSp;

        return enemy;
    }

    // nStatusの回復処理
    void recoverCharaMemStatus(){
        for (int i = 0; i < charaMem.size(); i++) {
            Character chara = charaMem.get(i);  // 参照渡し
            chara.nHP = chara.maxHP;
            chara.nMP = chara.maxMP;
            chara.nAtk = chara.maxAtk;
            chara.nDef = chara.maxDef;
            chara.nSp = chara.maxSp;
        }
    }




    // 装備品の取得 ---------------------------------------------------
    Equip getEquip(String equipID){
        Equip equip = new Equip();
        String[] eInfo = getEquipInfo(equipID);

        equip.ID = eInfo[0];
        equip.name = eInfo[1];
        equip.img = eInfo[2];
        equip.explain = raw2message(eInfo[3]);
        equip.category = eInfo[4];
        equip.atkCate= eInfo[5];
        equip.price = str2int( eInfo[6]);
        equip.sellPrice = str2int( eInfo[7]);

        equip.eHP += str2int( eInfo[8]);
        equip.eMP += str2int( eInfo[9]);
        equip.eAtk += str2int( eInfo[10]);
        equip.eDef += str2int( eInfo[11]);
        equip.eSp += str2int( eInfo[12]);

        return equip;
    }

    // ============================== ファイル取得 ============================================================

    // 状態異常ファイルの取得  -----------------------------------------------
    String[] getAbnoInfo(String abnoID) {
        ArrayList<String> itemList = constFileData.get(3);  // 状態異常ファイルデータの取得 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        for (int i = 0; i < itemList.size(); i++) {
            String[] fileInfo = itemList.get(i).split(",");
            if (fileInfo[0].equals(abnoID) == false) continue; //IDが正しいか判定  // 空文字でも通さない
            if(checkGlobalFlag(fileInfo[1]) == false) continue; // 変化条件で分岐

            return fileInfo;
        }
        return null;
    }



    // アイテム・技能ファイル情報の取得  -----------------------------------------------
    String[] getSkillInfo(String skillID) {
        ArrayList<String> itemList = constFileData.get(0);  // skillファイルデータの取得 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        for (int i = 0; i < itemList.size(); i++) {
            String[] fileInfo = itemList.get(i).split(",");
            if (fileInfo[0].equals(skillID) == false) continue; //アイテム名が正しいか判定  // 空文字でも通さない
            if(checkGlobalFlag(fileInfo[1]) == false) continue; // スキル使用回数で分岐させる？キャラ一人の時しか無理

            return fileInfo;
        }
        return null;
    }


    // 装備ファイル情報の取得  -----------------------------------------------
    String[] getEquipInfo(String equipID) {
        ArrayList<String> itemList = constFileData.get(1);  // itemファイルデータの取得 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        for (int i = 0; i < itemList.size(); i++) {
            String[] fileInfo = itemList.get(i).split(",");
            if (fileInfo[0].equals(equipID) == false) continue; //アイテム名が正しいか判定  // 空文字でも通さない

            return fileInfo;
        }
        return null;
    }

    // 敵ファイル情報の取得  -----------------------------------------------
    String[] getEnemyInfo(String enemyID) {
        ArrayList<String> enemyList = constFileData.get(2);  // enemyファイルデータの取得 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        for (int i = 0; i < enemyList.size(); i++) {
            String[] fileInfo = enemyList.get(i).split(",");
            if (fileInfo[0].equals(enemyID) == false) continue; //敵が正しいか判定  // 空文字でも通さない

            return fileInfo;
        }
        return null;
    }

    // brを改行に変換する
    String raw2message(String raw) {
        String message = "";
        String[] r = raw.split("&");
        for (int i = 0; i < r.length; i++) {
            switch (r[i]) {
                case("story"):
                    message += story;
                    break;
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


    // ============================== グローバルフラグ操作 ============================================================
    void updateGlobalFlag(String fileflags) {  //例、int=stage=+=1&str=chara=die&unique=bel=+=medicine
        if (fileflags.length() == 0) return;    //操作がないときは戻る

        // フラグ変更を一つずつ反映する
        String flags[] = fileflags.split("&");
        for (int i = 0; i < flags.length; i++) {    //一つずつ処理
            String flagCond[] = flags[i].split("=");

            // int, str, uniqueでそれぞれ確かめる
            switch (flagCond[0]) {    //例、int=stage=+=1&str=chara=die&unique=bel=+=medicine
                case ("int"):
                    // フラグがなかったら、初期化して追加
                    if(intGFlag.containsKey(flagCond[1]) == false){
                        intGFlag.put(flagCond[1], 0);
                    }
                    // 演算処理
                    if (flagCond[2].equals("+")) {       // "+"のとき
                        int value = intGFlag.get(flagCond[1]);
                        value += Integer.valueOf(flagCond[3]);
                        intGFlag.put(flagCond[1], value);
                    } else if (flagCond[2].equals("-")) { // "-"のとき
                        int value = intGFlag.get(flagCond[1]);
                        value -= Integer.valueOf(flagCond[3]);
                        intGFlag.put(flagCond[1], value);
                    } else {                              // ただの"="のとき
                        intGFlag.put(flagCond[1], Integer.valueOf(flagCond[2])); //フラグを更新
                    }
                    break;
                case ("str"):
                    strGFlag.put(flagCond[1], flagCond[2]); //フラグを更新
                    break;
                case ("unique"):
                    updateUniqueGFlag(flagCond);
                    break;
                default:
                    break;
            }

        }
        return;
    }


    // グローバルフラグの条件を満たしているか判定する ------------------------
    boolean checkGlobalFlag(String fileflags) {  //例、int=stage=>=1&str=chara=die&unique=bel=medicine
        if (fileflags.length() == 0) return true;    //条件がないとき、trueを返す

        // 条件を確認する
        String flags[] = fileflags.split("&");      //複数条件を分割
        for (int i = 0; i < flags.length; i++) {    //一つずつ条件を確かめる
            String flagCond[] = flags[i].split("=");

            // int, str, uniqueでそれぞれ確かめる
            switch (flagCond[0]) {    //例、int=stage=>=1&str=chara=die&unique=bel=medicine
                case ("int"):   //int=stage=>=1
                    // "flag=!"を確かめる
                    if (flagCond[2].equals("!")) {                           // ! のとき
                        if (intGFlag.containsKey(flagCond[1])) return false;  // flagがあれば false
                        else break;                                     // flagがなければ次の条件を調べる
                    }   // ! でないとき
                    else if (intGFlag.containsKey(flagCond[1]) == false)
                        return false;  // flagがなければ false

                    // 条件比較
                    if (flagCond[2].equals(">")) {       // ">"の判定
                        if (intGFlag.get(flagCond[1]) > Integer.valueOf(flagCond[3])) break;
                        else return false;
                    } else if (flagCond[2].equals("<")) { // "<"の判定
                        if (intGFlag.get(flagCond[1]) < Integer.valueOf(flagCond[3])) break;
                        else return false;
                    }   // "="の判定
                    else if (intGFlag.get(flagCond[1]) == Integer.valueOf(flagCond[2])) break;
                    else return false;
                case ("str"):    //str=chara=die
                    // "flag=!"を確かめる
                    if (flagCond[2].equals("!")) {                           // ! のとき
                        if (strGFlag.containsKey(flagCond[1])) return false;  // flagがあれば false
                        else break;                                     // flagがなければ次の条件を調べる
                    }   // ! でないとき
                    else if (strGFlag.containsKey(flagCond[1]) == false)
                        return false;  // flagがなければ false

                    // 条件比較
                    if (strGFlag.get(flagCond[1]).equals(flagCond[2])) break;    //条件を満たせば break
                    else return false;                                          //満たさなければ false

                case ("unique"):
                    if(checkUniqueGFlag(flagCond)) break;   //条件を満たせば break
                    else return false;
                default:    // フォーマットに合わなければ false
                    return false;

            }   // end switch()
        }   // end for
        //全ての条件を満たせば true
        return true;
    }

    // ユニークフラグの更新
    void updateUniqueGFlag(String[] flagCond){
        switch(flagCond[1]){
            case("chara"):  // unique=chara=recover
                // キャラに関する処理
                if(flagCond[2].equals("recover")){
                    // 全回復させる
                    for(int i=0; i<charaMem.size(); i++){
                        Character chara = charaMem.get(i);
                        chara.recoverStatus();
                    }
                }
                else{ // 特定キャラに対する処理
                    // unique=chara=セミ=escape=とんずら
                        Character chara = getChara(flagCond[2]);
                        switch (flagCond[3]) {
                            case ("escape"): //とんずら習得
                                chara.escape = setSkill(flagCond[4]);
                                break;
                            case("learn"):  // スキル習得
                                chara.skills.add(setSkill(flagCond[4]));
                                break;
                            default:
                                break;
                        }// end switch()
                    break;
                }
            break;
            case("money"):  // unique=money=+=1
                // お金の処理
                if (flagCond[2].equals("+")) {       // "+"のとき
                    money += Integer.valueOf(flagCond[3]);
                    charaMem.get(0).experience += Integer.valueOf(flagCond[3]); // ゲームバランス調整のために総取得 結晶数を数える
                } else if (flagCond[2].equals("-")) { // "-"のとき
                    money -= Integer.valueOf(flagCond[3]);
                } else {                              // ただの"="のとき
                    money = Integer.valueOf(flagCond[2]);
                }
                break;
            case("story"):  // unique=story=+=1
                // ゲーム進行度の処理
                if (flagCond[2].equals("+")) {       // "+"のとき
                    story += Integer.valueOf(flagCond[3]);
                } else if (flagCond[2].equals("-")) { // "-"のとき
                    story -= Integer.valueOf(flagCond[3]);
                } else {                              // ただの"="のとき
                    story = Integer.valueOf(flagCond[2]);
                }
                break;
            case("tmpLocal"):
                tmpLocal = flagCond[2];
                break;
            case("paragraph"):  // unique=paragraph=3
                paragraph = flagCond[2];
                break;
            case("page"):  // unique=page=mt
                page = flagCond[2];
                break;
        }
    }

    // ユニークフラグのチェック。一つの条件しかチェックしないので、満たした場合trueを返すことにする
    boolean checkUniqueGFlag(String[] flagCond){
        switch(flagCond[1]){
            // ある特定のキャラがある装備を持っているかの判定、超限定的
            case("chara"):  // unique=chara=セミ=weapon=エアソード
                    Character chara = getChara(flagCond[2]);    //キャラIDからキャラ入手
                    switch (flagCond[3]){
                        case("weapon"): //武器条件
                            if(chara.weapon.equals(flagCond[4]))    //武器名チェック
                                return true;
                            break;
                        default:
                            break;
                    }
                break;
            //お金があるかチェックする
            case("money"):  // unique=money=>=20
                // 条件比較
                if (flagCond[2].equals(">")) {       // ">"の判定
                    if ( money > Integer.valueOf(flagCond[3])) return true;
                    else return false;
                } else if (flagCond[2].equals("<")) { // "<"の判定
                    if ( money < Integer.valueOf(flagCond[3])) return true;
                    else return false;
                }   // "="の判定
                else if ( money == Integer.valueOf(flagCond[2])) return true;
                else return false;
            // 深度チェック
            case("story"):
                // 条件比較
                if (flagCond[2].equals(">")) {       // ">"の判定
                    if ( story > Integer.valueOf(flagCond[3])) return true;
                    else return false;
                } else if (flagCond[2].equals("<")) { // "<"の判定
                    if ( story < Integer.valueOf(flagCond[3])) return true;
                    else return false;
                }   // "="の判定
                else if ( story == Integer.valueOf(flagCond[2])) return true;
                else return false;
            case("tmpLocal"):   // unique=tmpLocal=win
                if ( tmpLocal.equals(flagCond[2])) return true;
                else return false;
            default:
                break;
        }//end switch()
        return false;
    }


}

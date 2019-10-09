package amai.box_world1;

import java.util.ArrayList;

/**
 * Created by Nakai Yoshiaki on 2017/12/01.
 */

public class Character {

    //キャラのステータス
    String ID = "";
    String name = "";
    String img = "";
    String btlBgImg = "";
    //String explain = "";
    //String putGFlag = "";

    //基礎値
    int HP=0;
    int MP=0;
    int atk=0;
    int def=0;
    int sp=0;

    //現在の能力値
    int nHP=0;
    int nMP=0;
    int nAtk=0; //攻防速も戦闘でダメージを受ける
    int nDef=0;
    int nSp=0;

    // 計算時の瞬間的な能力値
    //int tmpHP=0;
    //int tmpMP=0;
    int tmpAtk=0;
    int tmpDef=0;
    int tmpSp=0;
    float tmpInjuredMg= 1f;   // 被弾時倍率
    // int tmpRecoverMg

    //補正値を含めた最大能力値
    int maxHP=0;
    int maxMP=0;
    int maxAtk=0;
    int maxDef=0;
    int maxSp=0;

    // 戦闘処理用の変数
    int experience = 0; //経験値
    int escapeDifficult = 0;
    int actTime=0;   //
    int actCount=0; // 敵の行動指標。行動した回数
    String actionState = "";    // commandWait, actionWait      // コマンド入力待ち, 行動待ち,
    Skill btlsk;          //使用するスキル
    Character sklobj;     //スキル使用対象

    String btlBgm = "";
    int bgmRePos = 0;

    ArrayList<AbnormalState> btlstates = new ArrayList<>(); //状態異常


    //装備・スキル
    Equip weapon = new Equip();
    Equip protec = new Equip();
    //String weapon="";
    //String protec="";
    ArrayList<Skill> learnedAt = new ArrayList<>();    // 習得済みスキル
    ArrayList<Skill> learnedMg = new ArrayList<>();    // 習得済みスキル
    ArrayList<Skill> learnedGd = new ArrayList<>();    // 習得済みスキル
    ArrayList<Skill> learnedSp = new ArrayList<>();    // 習得済みスキル


    // 使用スキル
    Skill escape;
    ArrayList<Skill> skills = new ArrayList<>(); // 戦闘で使用できるスキル
    ArrayList<Integer> btlskPriority = new ArrayList<>(); // 敵のスキル使用優先度

    ArrayList<Skill> turnSkills = new ArrayList<>();     //指定ターン使用スキル
    ArrayList<Integer> btlskTurn = new ArrayList<>(); // スキル使用の指定ターン
    ArrayList<Skill> perTurnSkills = new ArrayList<>();  //ターン毎使用スキル
    ArrayList<Integer> btlskPerTurn = new ArrayList<>(); // スキル使用の指定ターン毎
    ArrayList<Skill> dependSkills = new ArrayList<>();   //相手依存の使用スキル
    ArrayList<String> btlskDependAtk = new ArrayList<>(); //
    ArrayList<String> btlskDependDef = new ArrayList<>(); //
    ArrayList<String> btlskDependWait = new ArrayList<>(); //
    ArrayList<String> btlskDependSpecial = new ArrayList<>(); //
    ArrayList<String> btlskDependMy = new ArrayList<>(); //

    //コンストラクタ
    public Character(){};   //引数なしのコンストラクタ


    // 回復する
    void recoverStatus(){
        calcEquipStatus();
        nHP = maxHP;
        nMP = maxMP;
        nAtk = maxAtk;
        nDef = maxDef;
        nSp = maxSp;
    }

    // ステータス値をチェックする
    void checkStatus(){
        if(nHP > maxHP){
            nHP = maxHP;
        }else if(nHP < 0){
            nHP = 0;
        }
        if(nMP > maxMP){
            nMP = maxMP;
        }else if(nMP < 0){
            nMP = 0;
        }
        if(nAtk > maxAtk){
            nAtk = maxAtk;
        }else if(nAtk < 0){
            nAtk = 0;
        }
        if(nDef > maxDef){
            nDef = maxDef;
        }else if(nDef < 0){
            nDef = 0;
        }
        if(nSp > maxSp){
            nSp = maxSp;
        }else if(nSp < 0){
            nSp = 0;
        }
    }


    // スキル -----------------------------------------------------------------------
    // スキル使用リソースが足りるかチェック
    boolean checkSklRes(){
        if(btlsk.spendnHP > nHP){
            return false;
        }
        if(btlsk.spendnMP > nMP){
            return false;
        }
        if(btlsk.spendnAtk > nAtk){
            return false;
        }
        if(btlsk.spendnDef > nDef){
            return false;
        }
        if(btlsk.spendnSp > nSp) {
            return false;
        }
        return true;
    }

    //
    String getShortageText(){
        String text = "";
        if(btlsk.spendnHP > nHP){
            text += "HPが足りない";
        }
        else if(btlsk.spendnMP > nMP){
            text += "MPが足りない";
        }
        else if(btlsk.spendnAtk > nAtk){
            text += "攻撃力が足りない";
        }
        else if(btlsk.spendnDef > nDef){
            text += "防御力が足りない";
        }
        else if(btlsk.spendnSp > nSp){
            text += "速さが足りない";
        }

        return text;
    }


    // 装備する ----------------------------------------------------------------
    void changeEquip(Equip eq) {
        //装備する
        switch (eq.category) { //分類
            case ("武器"):
                nHP -= weapon.eHP;
                nMP -= weapon.eMP;
                nAtk-= weapon.eAtk;
                nDef-= weapon.eDef;
                nSp -= weapon.eSp;
                weapon = eq;
                nHP += weapon.eHP;
                nMP += weapon.eMP;
                nAtk+= weapon.eAtk;
                nDef+= weapon.eDef;
                nSp += weapon.eSp;
                break;
            case ("防具"):
                nHP -= protec.eHP;
                nMP -= protec.eMP;
                nAtk-= protec.eAtk;
                nDef-= protec.eDef;
                nSp -= protec.eSp;
                protec = eq;
                nHP += protec.eHP;
                nMP += protec.eMP;
                nAtk+= protec.eAtk;
                nDef+= protec.eDef;
                nSp += protec.eSp;
                break;
        }

        //装備値の再計算
        calcEquipStatus();
        checkStatus();      // n値が0未満、max超えてないかチェック

        return;
    }


    // 装備能力値を反映する
    void calcEquipStatus() {

        // 最大値の計算
        maxHP = 0;
        maxMP = 0;
        maxAtk = 0;
        maxDef = 0;
        maxSp = 0;

        maxHP += HP + weapon.eHP + protec.eHP;    //基礎値 + 装備値
        maxMP += MP + weapon.eMP + protec.eMP;
        maxAtk += atk + weapon.eAtk + protec.eAtk;
        maxDef += def + weapon.eDef + protec.eDef;
        maxSp += sp + weapon.eSp + protec.eSp;
    }


    // 状態異常 ------------------------------------------------------------------
    // tmp値をセットする   // 状態異常メッセージを返す
    String setTmpStatus(String timing){
        //tmpHP = nHP;
        //tmpMP = nMP;
        tmpAtk = nAtk;
        tmpDef = nDef;
        tmpSp = nSp;
        tmpInjuredMg = 1;

        // 技待機の状態補正
        if(btlsk != null){
            //tmpAtk += btlsk.wAtk;
            tmpDef += btlsk.wDef;
        }

        String abnoMess = "";

        for(int i=0; i<btlstates.size(); i++){
            // 回復確認
            if(btlstates.get(i).checkRecover()){
                abnoMess += btlstates.get(i).recDispText;   //回復メッセージ
                btlstates.remove(i);    // 状態異常回復
                continue;
            }
            // 処理
            switch(timing){
                case("TimeCount"):  // timeカウント時の処理
                    if(btlstates.get(i).checkAbnoWhenTimeCount()){
                        abnoMess += btlstates.get(i).effDispText;   // 状態発動メッセージ
                    }
                    break;
                case("Injured"):    // 被弾時の処理
                    if(btlstates.get(i).checkAbnoWhenAttacked()){
                        abnoMess += btlstates.get(i).effDispText;
                    }
                    break;
                case("kaihuku"):    // 被弾時の処理
                    break;

                case("Act"):    //攻撃時の処理
                    if(btlstates.get(i).checkAbnoWhenAct()){
                        abnoMess += btlstates.get(i).effDispText;
                    }
                    break;
                case("wantoKnow"):  //tmp値を知りたいだけの時
                    btlstates.get(i).calcTmpEffect();
                    break;
            }
        }
        return abnoMess;
    }

    void recoverAllAbno() {
        for (int i = 0; i < btlstates.size(); i++) {
            btlstates.remove(i);    // 状態異常回復
        }
    }

}

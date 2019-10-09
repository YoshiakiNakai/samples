import java.util.ArrayList;

/**
 * Created by Nakai Yoshiaki on 2017/12/07.
 */

public class ISmenu extends GameState {

    Character chara;
    String menuState = "0"; // 開いたメニューの進行状態

    int page = 0;           // 成長メニューの項目ページ
    String pageStatus = "";// 成長メニューの選択ステータス

    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData){
        //this.eventList = extFileData.get(0);       //外部データの受け取り
        this.gcd = cd;                  //共通データの受け取り
        localState = gcd.tmpLocal;  // localStateの受け取り  // 開いたメニューの種類

        chara = gcd.charaMem.get(0);
        menuState = "0";
        page = 0;
        pageStatus = "";
    };

    //コンストラクタ
    public ISmenu(){
    }

    // 実行   ------------------------------------------------
    public void sExec(String btnText){
        if(btnText.length()!=0) this.actText = btnText;     //行動を保存

        // 開いたメニューの種類で分岐
        switch (localState) {
            case("装備"):
                equipMenu(btnText);
                break;
            case ("アイテム"):
                itemMenu(btnText);
                break;
            case ("技能"):
                skillMenu(btnText);
                break;
            case ("成長"):
                growMenu(btnText);
                break;
            default:
                selectMenu(btnText);
                break;
        }

    }

    // 開くメニュー選択 -----------------------
    void selectMenu(String btnText){
        if(localState.equals("アイテム") || localState.equals("技能") || localState.equals("成長")){
        }else {
            mainText = "項目を選んでください。";
            btnTexts[0] = "装備";
            btnTexts[1] = "技能";
            btnTexts[2] = "アイテム";
            btnTexts[3] = "成長";
            btnTexts[4] = "閉じる";

            localState = btnText;
            if (localState.equals("閉じる")) {
                gcd.nextState = "pop";
                return;
            }
        }

        // 開いたメニューの種類で分岐
        switch (localState) {
            case("装備"):
                equipMenu(btnText);
                break;
            case ("アイテム"):
                itemMenu(btnText);
                break;
            case ("技能"):
                skillMenu(btnText);
                break;
            case ("成長"):
                growMenu(btnText);
                break;
        }
    }


    // アイテムメニュー =====================================================================
    void itemMenu(String btnText){
        //メニューの進行状態で分岐
        switch(menuState){
            case "0":
                showItem();   //選択項目をボタンに表示する
                break;
            case "説明":
                explainItem(btnText);  // 説明文を表示する
                break;
            case "対象選択":
                useItem2obj(btnText);   // 使用対象判断
                break;
        }
    }

    // 選択項目をボタンに表示する--------------------
    void showItem() {

        mainText = "カバンの中を確かめる。";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";
        //次のstate
        menuState = "説明";

        // アイテムをボタンに表示
        for (int i = 0; i < gcd.bel.size(); i++) {
            btnTexts[i] = gcd.bel.get(i).name;
        }
    }

    // タッチした項目の説明文を表示する--------------------
    void explainItem(String btnText){

        if(btnText.equals("戻る")){ //戻るがタッチされていたら
            menuState = "0";
            localState = "";
            selectMenu("");
            //gcd.nextState = "pop";
            return;
        }/*
        if(btnText.equals("閉じる")){
            //menuState = "0";
            //localState = "";
            //selectMenu("");
            gcd.nextState = "pop";
            return;
        }*/


        // スキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=gcd.bel.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( gcd.bel.get(i).name.equals(actText)){
                btnIndex = i;
                break;
            }
        }
        chara.btlsk = gcd.setSkill( gcd.bel.get(btnIndex).ID);  // スキルのセット
        mainText = chara.btlsk.explain;
        //mainImg = tmpSki.img;
        btnTexts[0] = chara.btlsk.commandText;
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        // 使用可能か判定
        if(chara.checkSklRes() == false || chara.btlsk.category.equals("回復") == false){
            btnAbleFlag = "onlyRtn";   // 使用不可
        }else{
            btnAbleFlag = "";
        }
        menuState = "対象選択";
    }

    // アイテムを使用するか判断する--------------------
    void useItem2obj(String btnText){

        if(btnText.equals( chara.btlsk.commandText)) {
            //使用するとき
            gcd.doSkill(chara, chara);
            // アイテムの削除
            for (int i = 0; i < 4; i++) {
                if (gcd.bel.get(i).ID.equals(chara.btlsk.ID)) {
                    gcd.bel.remove(i);  //削除してから
                    showItem();          //残りアイテムの表示
                    //mainImg = tmpSki.img; // 使用アイテムのメッセージ表示
                    mainText = chara.btlsk.useDispText + "\n" + chara.btlsk.effDispText;
                    return;
                }
            }

        }
        else{ // 使用しないとき
            //項目表示に戻る
            showItem(); //menuState = "説明";
            btnAbleFlag = "";   // ボタンの状態を元に戻す
        }
    }


    // 技能メニュー   =========================================================================
    void skillMenu(String btnText){
        switch(menuState){
            case "0":
                showSkill();   //選択項目をボタンに表示する
                break;
            case "説明":
                explainSki(btnText);  // 説明文を表示する
                break;
            case "対象選択":
                useSkl2obj(btnText);   // 使用対象判断
                break;
        }
    }

    // 選択項目をボタンに表示する--------------------
    void showSkill() {

        mainText = "覚えている技能の効果を確認できます。";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";
        //次のstate
        menuState = "説明";

        // 覚えている技をボタンに表示する
        for (int i = 0; i < chara.skills.size(); i++) {
            btnTexts[i] = chara.skills.get(i).name;
        }
    }

    // タッチした項目の説明文を表示する--------------------
    void explainSki(String btnText){

        if(btnText.equals("戻る")){ //戻るがタッチされていたら
            menuState = "0";
            localState = "";
            selectMenu("");
            //gcd.nextState = "pop";
            return;
        }/*
        if(btnText.equals("閉じる")){
            //menuState = "0";
            //localState = "";
            //selectMenu("");
            gcd.nextState = "pop";
            return;
        }*/


        // スキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=chara.skills.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( chara.skills.get(i).name.equals(actText)){
                btnIndex = i;
                break;
            }
        }
        chara.btlsk = gcd.setSkill(chara.skills.get(btnIndex).ID);  // スキルのセット
        mainText = chara.btlsk.explain;
        //mainImg = tmpSki.img;
        btnTexts[0] = chara.btlsk.commandText;
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        // 使用可能か判定
        if(chara.checkSklRes() == false || chara.btlsk.category.equals("回復") == false){
            btnAbleFlag = "onlyRtn";   // 使用不可
        }else{
            btnAbleFlag = "";
        }
        menuState = "対象選択";
    }


    // 技能を使用するか判断する--------------------
    void useSkl2obj(String btnText){


        if(btnText.equals( chara.btlsk.commandText)){

            // 消費リソースが足りるかチェック
            if(chara.checkSklRes() == false){
                mainText = chara.getShortageText();
                return;
            }
            //使用する
            gcd.doSkill(chara, chara);
            //mainImg = tmpSki.img;
            showSkill(); //menuState = "説明";    // 表示メッセージを初期化した後に
            mainText = chara.btlsk.useDispText + "\n" + chara.btlsk.effDispText;   // 使用メッセージの表示

        }else{  //戻る
            //項目表示に戻る
            showSkill();
            btnAbleFlag = "";   // ボタンの状態を元に戻す
        }
    }

    // 装備メニュー========================================================================
    void equipMenu(String btnText){
        switch (menuState) {
            case("0"):
                showEquipItem();
                break;
            case("説明"):
                explainEquip(btnText);
                break;
        }
    }

    void showEquipItem(){
        btnTexts[0] = chara.weapon.name;
        btnTexts[1] = "";//chara.protec;
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";
        mainText = "装備の確認ができます。\n注意 装備は一つしか持てません。";

        menuState = "説明";
    }

    void explainEquip(String btnText){
        if(btnText.equals("戻る")){
            //gcd.nextState = "pop";
            menuState = "0";
            localState = "";
            selectMenu("");
            return;
        }
        Equip tmpEquip = gcd.getEquip(chara.weapon.ID);
        mainText = tmpEquip.explain;
        mainText += "\n攻 " + tmpEquip.eAtk + " / 防 " + tmpEquip.eDef + " / 速 " + tmpEquip.eSp;
        if(chara.maxMP > 0) mainText += " / MP " + tmpEquip.eMP;
    }

    // 成長メニュー========================================================================
    void growMenu(String btnText) {

        switch (menuState) {
            case("0"):
                showGrowItem();
                break;
            case("説明"):
                explainGrow(btnText);
                break;
            case("レベルアップ"):
                judgeLevelUp(btnText);
                break;
        }
    }

    void showGrowItem(){
        switch(page){
            case(0):
                mainText = "結晶の欠片を消費して成長できます。\n成長させたい能力を選んでください。";
                btnTexts[0] = "攻撃力";
                btnTexts[1] = "防御力";
                btnTexts[2] = "速さ";
                btnTexts[3] = "次へ";
                btnTexts[4] = "やめる";
                menuState = "説明";
                break;
            case(1):
                mainText = "結晶の欠片を消費して成長できます。\n成長させたい能力を選んでください。";
                btnTexts[0] = "HP";
                btnTexts[1] = "";
                if(chara.maxMP > 0) btnTexts[1] = "MP"; // maxMP>0が解放条件
                btnTexts[2] = "";
                btnTexts[3] = "前へ";
                btnTexts[4] = "やめる";
                menuState = "説明";
                break;
        }
    }

    // 説明-----------------------------------------------------------------
    void explainGrow(String btnText){

        btnTexts[0] = "レベルアップ";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        int[] MoneyValue = {0,0};

        switch(btnText){
            case("やめる"):    //
                //gcd.nextState = "pop";
                menuState = "0";
                localState = "";
                selectMenu("");
                break;
            case("次へ"):
                page = 1;
                showGrowItem(); //ボタンテキスト上書き
                break;
            case("前へ"):
                page = 0;
                showGrowItem(); //ボタンテキスト上書き
                break;

            // 処理
            case("HP"):
                MoneyValue = getLevelupMoneyValue(btnText);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "HPが " + MoneyValue[1] + " 上がります";
                pageStatus = btnText;
                menuState = "レベルアップ";
                break;
            case("MP"):
                MoneyValue = getLevelupMoneyValue(btnText);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "MPが " + MoneyValue[1] + " 上がります";;
                pageStatus = btnText;
                menuState = "レベルアップ";
                break;
            case("技能"):
                break;
            case("攻撃力"):
                MoneyValue = getLevelupMoneyValue(btnText);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "攻撃力が " + MoneyValue[1] + " 上がります";;
                pageStatus = btnText;
                menuState = "レベルアップ";
                break;
            case("防御力"):
                MoneyValue = getLevelupMoneyValue(btnText);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "防御力が " + MoneyValue[1] + " 上がります";;
                pageStatus = btnText;
                menuState = "レベルアップ";
                break;
            case("速さ"):
                MoneyValue = getLevelupMoneyValue(btnText);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "速さが " + MoneyValue[1] + " 上がります";;
                pageStatus = btnText;
                menuState = "レベルアップ";
                break;
        }
    }


    // レベルアップ処理-----------------------------------------------------------------
    void judgeLevelUp(String btnText){

        if( btnText.equals("やめる") || btnText.equals("戻る")){
            showGrowItem();
            return;
        }

        int[] MoneyValue = {0, 0};
        // 処理する
        switch(pageStatus){
            case("HP"):
                MoneyValue = getLevelupMoneyValue(pageStatus);
                // 消費リソースが足りるかチェックする
                if( gcd.money < MoneyValue[0]){
                    mainText = "結晶が足りない";
                    return;
                }
                // 処理する
                gcd.money -= MoneyValue[0];
                chara.HP += MoneyValue[1];  //基礎値
                chara.nHP += MoneyValue[1]; //現在値
                chara.calcEquipStatus();      //最大値
                gcd.updateGlobalFlag("int=" + pageStatus + "=+=1"); // レベルを上げる  // int=HP=+=1

                // 文章表示
                MoneyValue = getLevelupMoneyValue(pageStatus);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "HPが " + MoneyValue[1] + " 上がります";;

                break;
            case("MP"):
                MoneyValue = getLevelupMoneyValue(pageStatus);
                // 消費リソースが足りるかチェックする
                if( gcd.money < MoneyValue[0]){
                    mainText = "結晶が足りない";
                    return;
                }
                // 処理する
                gcd.money -= MoneyValue[0];
                chara.MP += MoneyValue[1];
                chara.nMP += MoneyValue[1];
                chara.calcEquipStatus();
                gcd.updateGlobalFlag("int=" + pageStatus + "=+=1"); // レベルを上げる  // int=HP=+=1

                MoneyValue = getLevelupMoneyValue(pageStatus);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "MPが " + MoneyValue[1] + " 上がります";
                break;
            case("技能"):
                break;
            case("攻撃力"):
                MoneyValue = getLevelupMoneyValue(pageStatus);
                // 消費リソースが足りるかチェックする
                if( gcd.money < MoneyValue[0]){
                    mainText = "結晶が足りない";
                    return;
                }
                // 処理する
                gcd.money -= MoneyValue[0];
                chara.atk += MoneyValue[1];
                chara.nAtk += MoneyValue[1];
                chara.calcEquipStatus();
                gcd.updateGlobalFlag("int=" + pageStatus + "=+=1"); // レベルを上げる  // int=HP=+=1

                MoneyValue = getLevelupMoneyValue(pageStatus);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "攻撃力が " + MoneyValue[1] + " 上がります";
                break;
            case("防御力"):
                MoneyValue = getLevelupMoneyValue(pageStatus);
                // 消費リソースが足りるかチェックする
                if( gcd.money < MoneyValue[0]){
                    mainText = "結晶が足りない";
                    return;
                }
                // 処理する
                gcd.money -= MoneyValue[0];
                chara.def += MoneyValue[1];
                chara.nDef += MoneyValue[1];
                chara.calcEquipStatus();
                gcd.updateGlobalFlag("int=" + pageStatus + "=+=1"); // レベルを上げる  // int=HP=+=1

                MoneyValue = getLevelupMoneyValue(pageStatus);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "防御力が " + MoneyValue[1] + " 上がります";
                break;
            case("速さ"):
                MoneyValue = getLevelupMoneyValue(pageStatus);
                // 消費リソースが足りるかチェックする
                if( gcd.money < MoneyValue[0]){
                    mainText = "結晶が足りない";
                    return;
                }
                // 処理する
                gcd.money -= MoneyValue[0];
                chara.sp += MoneyValue[1];
                chara.nSp += MoneyValue[1];
                chara.calcEquipStatus();
                gcd.updateGlobalFlag("int=" + pageStatus + "=+=1"); // レベルを上げる  // int=HP=+=1

                MoneyValue = getLevelupMoneyValue(pageStatus);
                mainText = "結晶を " + MoneyValue[0] + " 消費して";
                mainText += "速さが " + MoneyValue[1] + " 上がります";
                break;
        }
    }

    int[] getLevelupMoneyValue(String status){
        // 成長値
        int spendMoney = 0;
        int upValue = 0;

        switch(status){
            case("HP"):
                spendMoney = getGrowLevel(status)+1;
                if( getGrowLevel(status)+1 > 10){
                    upValue = 10;
                }else{
                    upValue = getGrowLevel(status)+1;
                }
                break;
            case("MP"):
                spendMoney = getGrowLevel(status)+1;
                upValue = 1;
                break;
            case("技能"):
                break;
            case("攻撃力"):
                spendMoney = getGrowLevel(status)+1;
                upValue = 1;
                break;
            case("防御力"):
                spendMoney = getGrowLevel(status)+1;
                upValue = 1;
                break;
            case("速さ"):
                spendMoney = getGrowLevel(status)+4;
                upValue = 1;
                break;
        }

        int[] mv = { spendMoney, upValue};
        return mv;
    }

    int getGrowLevel(String name){
        if(gcd.intGFlag.containsKey(name)){
            // 値があるとき
            return gcd.intGFlag.get(name);
        }else{//値がないとき
            return 0;
        }
    }

}

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nakai Yoshiaki on 2017/12/07.
 */

public class ISshop extends GameState {

    ArrayList<String> items;           // 商品項目
    ArrayList<String> itemsFileInfo;  // 商品情報
    String rndFlag = "";    // rnd選択するか否か
    String shopState = "";
    int lookIndex = -1;       // 見ている商品

    Character chara;
    Equip tmpEquip;
    Skill tmpItem;

    //外部ファイルデータ
    ArrayList<String> shopFile = new ArrayList<>();
    ArrayList<String> selectionFile = new ArrayList<>();

    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData) {
        this.shopFile = extFileData.get(0);       //外部データの受け取り
        this.selectionFile = extFileData.get(1);
        this.gcd = cd;
        localState = gcd.tmpLocal;  //店名

        // UI処理
        this.canvasFlag = "object"; //canvas描画方法の変更

        // 初期化
        items = new ArrayList<>();
        itemsFileInfo = new ArrayList<>();
        setItems();
        rndFlag = "";
        shopState = "0";
        lookIndex = -1;
        chara = gcd.charaMem.get(0);
        tmpItem = null;
        tmpEquip = null;
    }

    // 処理する
    void sExec(String btnText) {
        if (btnText.length() != 0) this.actText = btnText;     //行動を保存
        switch(shopState){
            case("0"):
                showItems();    // 商品表示
                break;
            case("select"):
                explainItem(btnText);   // 商品説明
                break;
            case("YesNo"):
                buy(btnText);      //購入処理
                break;
        }
    }

    // 再実行 前処理 ------------------------------------------------
    @Override
    public void sReStart(String btnText){
        /*if(tmpLocal.equals("pop")) {
            // popさせる
            gcd.nextState = "pop";
            return;
        }*/
    }


    // 商品を表示する ----------------------------------------------
    void showItems(){

        lookIndex = -1; //商品index初期化
        setShopFile();  // shopファイル処理

        // 初期状態の再現
        shopState = "select";
        //btnTexts[0] = "";   // shopファイル情報を上書き
        //btnTexts[1] = "";
        //btnTexts[2] = "";
        //btnTexts[3] = "";
        //btnTexts[4] = "立ち去る";
        // 商品表示
        for(int i=0; i<items.size(); i++){
            btnTexts[i] = items.get(i); // 登録項目をボタンに表示
        }
    }

    // shopファイルのparse ----------------------------------------
    void setShopFile(){
        // 店ファイルのparse
        for(int i=0; i<shopFile.size(); i++) {
            //ファイルを一行ずつチェックしていく
            String fileInfo[] = shopFile.get(i).split(",");
            if(checkLocalState(fileInfo[0]) == false) continue;        //local判定
            if(gcd.checkGlobalFlag(fileInfo[1]) == false) continue;    //Flag判定

            mainImg = fileInfo[2];   // 背景
            objectImg = fileInfo[3]; // 前面表示画像
            mainText = fileInfo[4];
            rndFlag = fileInfo[5];

            btnTexts[0] = fileInfo[6];
            btnTexts[1] = fileInfo[7];
            btnTexts[2] = fileInfo[8];
            btnTexts[3] = fileInfo[9];
            btnTexts[4] = fileInfo[10];

            gcd.updateGlobalFlag(fileInfo[11]);
            gcd.nextState = fileInfo[12];
            gcd.tmpLocal = fileInfo[13];
            gcd.extFileNames = fileInfo[14];

            break;
        }
    }

    // 商品説明     // selectionファイル処理 -------------------------------------------------
    void explainItem(String btnText){
        //戻るがタッチされていたら
        if(btnText.equals("戻る") || btnText.equals("立ち去る")){
            gcd.nextState = "pop";
            return; // 戻る
        }

        // インデックスの取得
        for(int i=items.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( items.get(i).equals(btnText)){
                lookIndex = i;
                break;
            }
        }

        if(lookIndex == -1){
            // タッチしたボタンが登録商品でないなら
            localState = btnText;   // ボタン名をlocalStateにして
            showItems();          // shopファイル検索・適用・商品で上書き
            return;
        }

        String[] ifi = itemsFileInfo.get(lookIndex).split(",");     // <<<<<外部ファイル情報<<<<<<

        mainText = "";
        // 商品解説取得
        if( ifi[5].length() != 0) {
            mainImg = ifi[5];
        }
        if( ifi[6].length() != 0) {
            mainText += ifi[6] + "\n";
        }
        // 検索先skillファイル、equipファイルで分岐
        switch (ifi[3]){
            case("武器"):
            case("防具"):
                tmpItem = null; // この後の処理でnullかどうかで処理を分岐する
                tmpEquip = gcd.getEquip(ifi[0]);
                mainText += tmpEquip.name + "  " + tmpEquip.explain;
                mainText += "\n価格: " + tmpEquip.price + "欠片";
                mainText += " / 攻 " + tmpEquip.eAtk + " / 防 " + tmpEquip.eDef + " / 速 " + tmpEquip.eSp;
                if(chara.maxMP > 0) mainText += " / MP " + tmpEquip.eMP;
                break;
            case("アイテム"):
                tmpItem = gcd.setSkill(ifi[0]);
                tmpEquip = null;
                // 価格に補正をかける
                if(tmpItem.price > 1) { // 1結晶は常に1結晶
                    int x = (int)(gcd.story/100) - 1;   //<<<<<<< 値段 <<<<<<<<<  //敵の半分の価格
                    tmpItem.price += Math.pow(2, x);
                }
                mainText += tmpItem.name + "  " + tmpItem.explain;
                mainText += "\n価格: " + tmpItem.price + "欠片";
                break;
        }


        btnTexts[0] = "買う";
        btnTexts[1] = "買わない";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "";
        shopState = "YesNo";
    }

    // 購入処理------------------------------------------------
    void buy(String btnText){
        if(btnText.equals("買わない") || btnText.equals("いいえ")){
            showItems();
            return;
        }
        // 買う、はい、のとき進む

        // 購入処理
        if(tmpEquip != null){
            // 装備購入処理---------------------
            // 購入可能か判定
            if(gcd.money < tmpEquip.price){
                mainText = "お金が足りない";
                return;
            }
            // お金を減らす
            gcd.money -= tmpEquip.price;
            // 装備する
            chara.changeEquip(tmpEquip);
            purchaseEvent();     // 購入時の特殊処理
            // 商品を削除する
            items.remove(lookIndex);
            itemsFileInfo.remove(lookIndex);
            showItems();

        }else{  // tmpItem != null
            // アイテム購入処理------------------
            // 購入可能か判定
            if(gcd.money < tmpItem.price){
                mainText = "お金が足りない";
                return;
            }
            // アイテム数の確認をする
            if(makeSureItemsNum(btnText)){
                return;
            }

            // お金を減らす
            gcd.money -= tmpItem.price;
            //アイテム追加
            gcd.bel.add(tmpItem);
            purchaseEvent();     // 購入時の特殊処理
            // 商品を削除する
            items.remove(lookIndex);
            itemsFileInfo.remove(lookIndex);
            showItems();

        }
    }

    // アイテム数の確認         // trueで4つ以上
    boolean makeSureItemsNum(String btnText){
        if(btnText.equals("はい")) return false;

        if(gcd.bel.size() == 4){
            mainText = "アイテムは4つまでしか持つことができない。\nそれでも購入しますか？";
            btnTexts[0] = "はい";
            btnTexts[1] = "いいえ";
            btnTexts[2] = "";
            btnTexts[3] = "";
            btnTexts[4] = "";
            return true;
        }
        return false;
    }

    // 購入時特殊処理  // selectionファイル処理----------------------------------------
    void purchaseEvent(){
        String[] ifi = itemsFileInfo.get(lookIndex).split(",");     // <<<<<外部ファイル情報<<<<<<
        gcd.updateGlobalFlag(ifi[7]);   // グローバルフラグ更新
        if(ifi[8].length() != 0) {
            localState = ifi[8];    // 店の状態を変化させる
        }else{
            localState = "afterPurchase";
        }
    }

    // 商品を初期化する ------------------------------------------------------------
    void setItems(){
        for(int i=0; i<shopFile.size(); i++) {
            //actionファイルを一行ずつチェックしていく
            String fileInfo[] = shopFile.get(i).split(",");
            if(checkLocalState(fileInfo[0]) == false) continue;        //local判定
            if(gcd.checkGlobalFlag(fileInfo[1]) == false) continue;    //Flag判定

            // 商品のセット
            setShopFile();
            for(int btn=0; btn<btnTexts.length-1; btn++){
                // 商品名取得
                items.add( fileInfo[btn+6]);    // <<<<<<<<<< shopファイル処理、商品登録 <<<<<<<<<<
            }

            // rnd選択を行うか判定する
            if(rndFlag.equals("on")){
                break; // rnd選択へ移る
            }else{
                return; // 戻る
            }
        }

        // 続けて rnd選択を行う // selectionファイル処理-------------------------
        for(int btn=0; btn<btnTexts.length-1; btn++) {
            if(items.get(btn).length() == 0) continue;   // 空文字のときは処理しない

            // rnd選択処理を行う
            ArrayList<String> rndList = new ArrayList<>();     // rndイベント格納
            ArrayList<String> itemsInfo = new ArrayList<>();     // その分類
            ArrayList<Integer> priority = new ArrayList<>();   // rnd優先度
            int prioritySum = 0;    // rnd優先度合計値

            //rndイベント取得
            for (int i = 0; i < selectionFile.size(); i++) {
                String fileInfo[] = selectionFile.get(i).split(",");
                if (gcd.checkGlobalFlag(fileInfo[2]) == false) continue;
                if (checkRndCategory(items.get(btn), fileInfo[3], fileInfo[4]) == false) continue;   // rnd分類、優先度 判定

                rndList.add(fileInfo[1]);                   // local値候補を追加
                itemsInfo.add(selectionFile.get(i));        // ファイル情報を丸ごと入れる
                priority.add(Integer.valueOf(fileInfo[4]));  //優先度
                prioritySum += Integer.valueOf(fileInfo[4]);
            }

            // rndイベント選択
            if(prioritySum == 0) break;  // rnd分類に該当しなければ戻る
            Random rand = new Random();
            int r = rand.nextInt(prioritySum);

            int x = 0;
            for (int i = 0; i < priority.size(); i++) {
                x += priority.get(i);
                if (r < x) {
                    items.set(btn, rndList.get(i)); // 商品名
                    itemsFileInfo.add( itemsInfo.get(i));   // 商品情報
                    break;
                }
            }
        }
    }

    // rnd分類判定 ------------------------------------------------
    boolean checkRndCategory(String item, String fileCate, String filePriority){

        if(fileCate.equals(item) || fileCate.length()==0) {  // rnd分類判定、空文字は通す、ただし
            if(filePriority.length() != 0) {                    // 優先度があるか確認する
                return true;
            }
        }
        return false;
    }


}

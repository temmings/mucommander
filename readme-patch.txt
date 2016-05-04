--------------------------------------------------------------------------------
■muCommanderとは
--------------------------------------------------------------------------------
いわゆるファイル管理ソフトです。細かい分類ではキーボード主体の2画面ファイラー。
http://www.mucommander.com/ にて配布されています。

--------------------------------------------------------------------------------
■本家からの修正内容
--------------------------------------------------------------------------------
大まかに言うと、muCommanderに以下の修正を加えたモジュールを配布しています。

・インクリメンタルサーチ(QuickSearch)をMigemo対応にしました。
・初期状態でインクリメンタルサーチを無効にして通常のアルファベットキーをほかの機能に割り当てられるようにしました。
・圧縮ファイル .lzh に対応

--------------------------------------------------------------------------------
■Download
--------------------------------------------------------------------------------
以下ページで取得してください
http://blog.marupara.dyndns.org/mucommander/download.html

--------------------------------------------------------------------------------
■使い方 (すでにmucommander入れてる人向け)
--------------------------------------------------------------------------------
1, http://0xcc.net/migemo/ へアクセスし、下の方からmigemo-*.tar.gz をダウンロードする
2, migemo-*.tar.gzを解凍し、中の migemo-dict をホームディレクトリ
   (WindowsだとC:Document and Settings\<ログインしたユーザー名>)にある
   .mucommander ディレクトリの中に置く
3, mucommanderに元からついてるmucommander.jarをmucommander.jar-orgにリネーム
4, mucommander-lei-<リリース日>.zip に入ってるmucommander.jarを変わりに置いてやる
5, 起動

--------------------------------------------------------------------------------
■使い方 (まだmucommander入れてない人向け)
--------------------------------------------------------------------------------
1, http://www.mucommander.com/ へアクセスして各環境のmucommander本体をダウンロードする
2, mucommanderをインストールする
3, http://0xcc.net/migemo/ へアクセスし、下の方からmigemo-*.tar.gz をダウンロードする
4, migemo-*.tar.gzを解凍し、中の migemo-dict をホームディレクトリ
   (WindowsだとC:Document and Settings\<ログインしたユーザー名>)にある
   .mucommander ディレクトリの中に置く
5, mucommanderに元からついてるmucommander.jarをmucommander.jar-orgにリネーム
6, mucommander-lei-<リリース日>.zip に入ってるmucommander.jarを変わりに置いてやる
7, 起動


--------------------------------------------------------------------------------
■修正点
--------------------------------------------------------------------------------
○クイックサーチ関連
    ・クイックサーチ開始アクション StartQuickSearchAction 追加。 このアクションがaction_keymap.xmlに登録されている場合、このキー以外でクイックサーチが開始されなくなる。
    ・クイックサーチの対象から 親フォルダ ".." を除外
    ・クイックサーチの有効キーからスペースとスラッシュ'/'を除外
    ・クイックサーチ中のスペースキーをマーク動作に変更 (ディレクトリ一括マークの変わりに使ってください）
    ・action-keymap.xmlでキー"A～Z0-9"や一部の記号などに機能を割り当てていても、クイックサーチ中は無視するようにした
    ・クイックサーチモードと非クイックサーチモードの切り替えアクション SwapQuickSearchModeAction 追加。
      クイックサーチモード中は基本的なキー入力はクイックサーチが優先されるモードと、クイックサーチを行わないモード。
      (深いディレクトリに入る時等、最初からクイックサーチのほうが楽な場合があるので作成しました）

○追加アクション
    ・カーソル移動アクション (Up|Down|RollUp|RollDown)CursorAction 追加
    ・アクティブパネル変更アクション Switch(Left|Right)ActiveTableAction 追加
    ・アクティブパネルのディレクトリを反対側と同じにするアクション SetSameFolderAsBothAction 追加

○（開く..）関連
    ・(開く..)のポップアップメニューを開くアクション OpenWithPopupMenuAction 追加
    ・(開く..)のコマンドリストの最初の文字でショートカット起動できるようにした
      さらにassociation.xmlにコマンドと拡張子の関連付けがある場合は指定されたコマンドのみ表示される。複数指定可能。
    ・(開く..)のコマンドリストの並び順を(おそらく)書いた順番に並ぶようにした

○ロケーションバー関連
    ・ロケーションバーからTABでファイル一覧に移動しないよう修正(ESCかリターンで抜けてください)
    ・ロケーションバーにて、TABでもオートコンプリートが動作するよう修正

○オートコンプリート関連
    ・TABまたはカーソル下でオートコンプリートが開始された場合は一番上を初期状態で選択するよう修正
    ・オートコンプリートで一番下と一番上の間をカーソルがループするよう修正

○その他
    ・コピー先ディレクトリが存在しない場合は自動的に作成するよう修正
    ・解凍する際の解凍先の初期値をファイル名のディレクトリつきに修正
    ・メニューがalt+キーで開くようにメニューの最初を英語にした(見た目悪いけど・・・)

--------------------------------------------------------------------------------
■その他、注意点とかメモ
--------------------------------------------------------------------------------
    ・association.xmlのcommand指定でcommands.xmlで宣言されていないalias名を書くと、それ以降が読み込まれない・[キーはOPEN_BRACKET
    ・,キーはCOMMA
    ・.キーはPERIOD。テンキーの.はDECIMAL
    ・/キーはSLASH
    ・クイックサーチで最初にｄを押した時の動きが悪いのはmigemo-dictのdに \. が含まれているため。(dotに反応する）
      動きが気になる場合は辞書からdotやdotsを削除してください
    ・shift ENTER の「自然に開く」ってのはシステム登録されているアプリで開くという意味だが、
      associations.xmlが優先されるためあんまり使えない。zipファイルとかをシステムに関連付けられてるアプリで開くくらい。
    ・右クリックのダイアログに出る謎のコマンド「開く...」はcommands.xmlで設定したコマンド一覧が出るところ。英語ではOpen with...
    ・preferences.xmlはmuCommander終了時に上書きされるので、修正するときはmucommanderを終了させてから編集しましょう。
    ・エディタはpreferences.xmlのeditor部分を修正することで変更できます。viewerも同じ手法で変更可能。
        &lt;editor&gt;
            &lt;use_custom&gt;true&lt;/use_custom&gt;
            &lt;custom_command&gt;c:\\ProgramFiles\\editor\\editor $f&lt;/custom_command&gt;
        &lt;/editor&gt;
    ・Eclipse環境で作業していて、直接実行するためにtmp/classesをクラスパスに追加している場合は定期的に削除しないと変なクラスが残ってたりするので注意

--------------------------------------------------------------------------------
■修正点メモ
--------------------------------------------------------------------------------
・jLha
～InputStream.java系のcloseを2回以上呼ぶとNullPointerExceptionを吐くのでnullチェックを入れた

--------------------------------------------------------------------------------
■ライセンス
--------------------------------------------------------------------------------
オリジナル通り GPL v3 になります。
Copyright (c) 2008 Lei <http://marupara.dyndns.org>

--------------------------------------------------------------------------------
■使用ライブラリなどのライセンス表示
--------------------------------------------------------------------------------
muCommander   (GPL v3)
Copyright (C) 2002-2008 Maxence Bernard http://www.mucommander.com/
詳細はlicense.txt参照

----------------------------------------
j/Migemo   (MIT/X)

Copyright (c) 2008 n|a <http://v2c.s50.xrea.com/jmigemo/>

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

----------------------------------------
Migemo
http://0xcc.net/migemo/
ライセンスが良く分からないので同梱せず

----------------------------------------
jlha
http://homepage1.nifty.com/dangan/

Copyright (C) 2002  Michel Ishizuka  All rights reserved.

以下の条件に同意するならばソースとバイナリ形式の再配布と使用を
変更の有無にかかわらず許可する。

１．ソースコードの再配布において著作権表示と この条件のリスト
    および下記の声明文を保持しなくてはならない。

２．バイナリ形式の再配布において著作権表示と この条件のリスト
    および下記の声明文を使用説明書もしくは その他の配布物内に
    含む資料に記述しなければならない。

このソフトウェアは石塚美珠瑠によって無保証で提供され、特定の目
的を達成できるという保証、商品価値が有るという保証にとどまらず、
いかなる明示的および暗示的な保証もしない。
石塚美珠瑠は このソフトウェアの使用による直接的、間接的、偶発
的、特殊な、典型的な、あるいは必然的な損害(使用によるデータの
損失、業務の中断や見込まれていた利益の遺失、代替製品もしくは
サービスの導入費等が考えられるが、決してそれだけに限定されない
損害)に対して、いかなる事態の原因となったとしても、契約上の責
任や無過失責任を含む いかなる責任があろうとも、たとえそれが不
正行為のためであったとしても、またはそのような損害の可能性が報
告されていたとしても一切の責任を負わないものとする。


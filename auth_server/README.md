#auth_server
spring-security-oauth2-authorization-server をベースにした IdP のサンプルです。

## ローカル起動
mvn から　SpringBoot を起動するコマンドにて起動可能です。 起動ポートは設定ファイルに記載していますが、８０９０となっています。

### 設定ファイル
src/main/resources 配下にある　config ディレクトリ、config-local ディレクトリに格納しています。
起動ポートは 8080 となっています。

### 起動コマンド
$ mvn clean spring-boot:run

## 機能群
### 情報差分登録
OAuth2AuthenticationSuccessHandlerExt により情報登録状態を判定
kk.sample.auth_server.preference パッケージ下のクラスにより情報登録状態の設定ハンドリングを行う

⬛︎ oidc レスポンスにリソースサーバの権限
⬛︎ グループバリデーション
	PreferenceServletModelAttributeMethodProcessor により
	ServletModelAttributeMethodProcessor を置換
⬜︎ クライアントID によって権限変更できる様にする？
	カスタマ向けはいらない気がする
⬜︎ ログアウト伝搬
⬛︎ ロック機構
	UserAccountLockService にて実装
　　ログイン ID だけが対象なので拡張が必要かも (IP とか)

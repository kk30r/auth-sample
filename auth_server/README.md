# auth_server  
spring-security-oauth2-authorization-server をベースにした IdP のサンプルです。  

## ローカル起動  
mvn から　SpringBoot を起動するコマンドにて起動可能です。  
起動ポートは設定ファイルに記載していますが、８０９０となっています。  

### 設定ファイル  
src/main/resources 配下にある　config ディレクトリ、config-local ディレクトリに格納しています。  
起動ポートは 8080 となっています。  

### 起動コマンド  
$ mvn clean spring-boot:run  

## 機能群  
### 情報差分登録  
kk.sample.auth_server.preference パッケージ下のクラスにより情報登録状態の設定ハンドリングを行う  
OAuth2AuthenticationSuccessHandlerExt により情報登録状態を判定  

### oidc レスポンスにリソースサーバの権限を追加
OAuth2TokenContextService の内部処理により OidcUserInfo に "authorities" を設定　　　
SecurityConfigExt#jwtAuthenticationConverter により上記で設定された "authorities" を参照する様に設定　　　

### バリデーション  
PreferenceServletModelAttributeMethodProcessor により ServletModelAttributeMethodProcessor を置換　　
	
### ロック機構  
UserAccountLockService にて実装

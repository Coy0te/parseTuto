Il nous reste un concept important à aborder > avant de passer à la pratique : les objets implicites. Il en existe deux types : 


- ceux qui sont mis à disposition via la technologie JSP ;
- ceux qui sont mis à disposition via la technologie EL.


Du _**gras dans** de l'italique_.
De **_l'italique dans_ du gras**.
Et du ~~barre sans espac~~ es.

Comme vous devez maintenant savoir le faire, rendez-vous sur  [ `date` ](http://php.net/date)  pour avoir la description de la fonction.


[[information]]
| Si vous désirez utiliser des formats de dates et d'heures personnalisés, sachez que la syntaxe est celle de la fonction  [ `date()` ](http://php.net/manual/fr/function.date.php)  de PHP.



# Les objets de la technologie JSP

Pour illustrer ce nouveau concept, revenons sur la première JSP que nous avions écrite dans  [le chapitre sur la transmission des données](http://www.siteduzero.com/tutoriel-3-655546-transmission-de-donnees.html)  :


```jsp
<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Test</title>
    </head>
    <body>
        <p>Ceci est une page générée depuis une JSP.</p>
        <p>
            <% 
            String attribut = (String) request.getAttribute("test");
            out.println( attribut );
            %>
        </p>
    </body>
</html>
```

Je vous avais alors fait remarquer qu'à la ligne 13, nous avions directement utilisé l'objet **out** sans jamais l'avoir instancié auparavant. De même, à la ligne 12 nous accédions directement à la méthode  `request.getAttribute()`  sans jamais avoir instancié d'objet nommé **request**… 


[[question]]
| Comment est-ce possible ?

Pour répondre à cette question, nous devons nous intéresser une nouvelle fois au code de la servlet auto-générée par Tomcat, comme nous l'avions fait dans le second chapitre de cette partie. Retournons donc dans le répertoire **work** du serveur, qui rappelez-vous est subtilisé par Eclipse, et analysons à nouveau le code du fichier **test_jsp.java** :


```java
...

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!DOCTYPE html>\r\n");
      out.write("<html>\r\n");
      out.write("    <head>\r\n");
      out.write("        <meta charset=\"utf-8\" />\r\n");
      out.write("        <title>Test</title>\r\n");
      out.write("    </head>\r\n");
      out.write("    <body>\r\n");
      out.write("        <p>Ceci est une page générée depuis une JSP.</p>\r\n");
      out.write("        <p>\r\n");
      out.write("            ");
 
            String attribut = (String) request.getAttribute("test");
            out.println( attribut );
            
      out.write("\r\n");
      out.write("        </p>\r\n");
      out.write("    </body>\r\n");
      out.write("</html>");
    } 
    ...
```

Analysons ce qui se passe dans le cas de l'objet **out** : 


- à la ligne 10, un objet nommé **out** et de type  [ `JspWriter` ](http://docs.oracle.com/javaee/6/api/javax/servlet/jsp/JspWriter.html)  est créé ;
- à la ligne 24, il est initialisé avec l'objet _writer_ récupéré depuis la réponse ;
- à la ligne 39, c'est tout simplement notre ligne de code Java, basée sur l'objet **out**, qui est recopiée telle quelle de la JSP vers la servlet auto-générée !

Pour l'objet **request**, c'est un peu différent. Comme je vous l'ai déjà expliqué dans le second chapitre, notre JSP est ici transformée en servlet. Si elle en diffère par certains aspects, sa structure globale ressemble toutefois beaucoup à celle de la servlet que nous avons créée et manipulée dans nos exemples jusqu'à présent. Regardez la ligne 3 : le traitement de la paire requête/réponse est contenu dans une méthode qui prend pour arguments les objets  `HttpServletRequest`  et  `HttpServletResponse` , exactement comme le fait notre méthode  `doGet()`  ! Voilà pourquoi il est possible d'utiliser directement les objets **request** et **response** depuis une JSP.


[[information]]
| Vous devez maintenant comprendre pourquoi vous n'avez pas besoin d'instancier ou de récupérer les objets **out** et **request** avant de les utiliser dans le code de votre JSP : dans les coulisses, **le conteneur s'en charge pour vous lorsqu'il traduit votre page en servlet** ! Et c'est pour cette raison que ces objets sont dits "implicites" : vous n'avez pas besoin de les déclarer de manière… explicite. Logique, non ? ;)


Par ailleurs, si vous regardez attentivement le code ci-dessus, vous constaterez que les lignes 6 à 13 correspondent en réalité toutes à des initialisations d'objets : **pageContext**, **session**, **application**… En fin de compte, le conteneur met à votre disposition toute une série d'objets implicites, tous accessibles directement depuis vos pages JSP. En voici la liste :


+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                          Identifiant                         |                        Type de l'objet                       |                                                                                                                         Description                                                                                                                         |
+==============================================================+==============================================================+=============================================================================================================================================================================================================================================================+
| **pageContext**  `PageContext`  | **pageContext**  `PageContext`  | Il fournit des informations utiles relatives au contexte d'exécution. Entre autres, il permet d'accéder aux attributs présents dans les différentes portées de l'application. Il contient également une référence vers tous les objets implicites suivants. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                              **application** |               `ServletContext`  |                                                                                                                          Il permet depuis une page JSP d'obtenir ou de modifier des informations relatives à l'application dans laquelle elle est exécutée. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                  **session** |                  `HttpSession`  |                                                                                                                     Il représente une session associée à un client. Il est utilisé pour lire ou placer des objets dans la session de l'utilisateur courant. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                  **request** |           `HttpServletRequest`  |                                                                                              Il représente la requête faite par le client. Il est généralement utilisé pour accéder aux paramètres et aux attributs de la requête, ainsi qu'à ses en-têtes. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                 **response** |          `HttpServletResponse`  |                                                                Il représente la réponse qui va être envoyée au client. Il est généralement utilisé pour définir le Content-Type de la réponse, lui ajouter des en-têtes ou encore pour rediriger le client. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                **exception** |                    `Throwable`  |                                                                                                                           Il est uniquement disponible dans les pages d'erreur JSP. Il représente l'exception qui a conduit à la page d'erreur en question. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                      **out** |                    `JspWriter`  |                                                                                                                               Il représente le contenu de la réponse qui va être envoyée au client. Il est utilisé pour écrire dans le corps de la réponse. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                   **config** |                `ServletConfig`  |                                                                                                                                                              Il permet depuis une page JSP d'obtenir les éventuels paramètres d'initialisation disponibles. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                     **page** |                  objet  `this`  |                                                 Il est l'équivalent de la référence  `this`  et représente la page JSP courante. Il est déconseillé de l'utiliser, pour des raisons de dégradation des performances notamment. |
+--------------------------------------------------------------+--------------------------------------------------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+


De la même manière que nous avons utilisé les objets **request** et **out** dans notre exemple précédent, il est possible d'utiliser n'importe lequel de ces neuf objets à travers le code Java que nous écrivons dans nos pages JSP…


[[question]]
| Hein ?! Encore du code Java dans nos pages JSP ?

Eh oui, tout cela est bien aimable de la part de notre cher conteneur, mais des objets sous cette forme ne vont pas nous servir à grand-chose ! Souvenez-vous : nous avons pour objectif de ne plus écrire de code Java directement dans nos pages.



# Les objets de la technologie EL

J'en vois déjà quelques-uns au fond qui sortent les cordes…  :euh:  Vous avez à peine digéré les objets implicites de la technologie JSP, je vous annonce maintenant qu'il en existe d'autres rendus disponibles par les expressions EL ! Pas de panique, reprenons tout cela calmement. En réalité, et heureusement pour nous, la technologie EL va apporter une solution élégante au problème que nous venons de soulever : **nous allons grâce à elle pouvoir profiter des objets implicites sans écrire de code Java** !

Dans les coulisses, le concept est sensiblement le même que pour les objets implicites JSP : il s'agit d'objets gérés automatiquement par le conteneur lors de l'évaluation des expressions EL, et auxquels nous pouvons directement accéder depuis nos expressions sans les déclarer auparavant. Voici un tableau des différents objets implicites mis à disposition par la technologie EL :


+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|          Catégorie          |      Identifiant      |                                                                                                                                   Description                                                                                                                                   |
+=============================+=======================+=================================================================================================================================================================================================================================================================================+
|                         JSP |       **pageContext** |                                                                                                                                                                                                               Objet contenant des informations sur 
l'environnement du serveur. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                     Portées |         **pageScope** |                                                                                                                                                              Une  `Map`  qui associe les noms et valeurs des attributs 
ayant pour portée la page. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                     Portées |      **requestScope** |                                                                                                                                                           Une  `Map`  qui associe les noms et valeurs des attributs 
ayant pour portée la requête. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                     Portées |      **sessionScope** |                                                                                                                                                           Une  `Map`  qui associe les noms et valeurs des attributs 
ayant pour portée la session. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                     Portées |  **applicationScope** |                                                                                                                                                        Une  `Map`  qui associe les noms et valeurs des attributs 
ayant pour portée l'application. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|       Paramètres de requête | Paramètres de requête |                                                                                                                                                                         Une  `Map`  qui associe les noms et valeurs des paramètres 
de la requête. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|       Paramètres de requête | Paramètres de requête |                                                                                                                       Une  `Map`  qui associe les noms et multiples valeurs ****** des paramètres 
de la requête sous forme de tableaux de String. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|         En-têtes de requête |            **header** |                                                                                                                                                                     Une  `Map`  qui associe les noms et valeurs des paramètres 
des en-têtes HTTP. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|         En-têtes de requête |      **headerValues** |                                                                                                                   Une  `Map`  qui associe les noms et multiples valeurs ****** des paramètres 
des en-têtes HTTP sous forme de tableaux de String. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                     Cookies |            **cookie** |                                                                                                                                                                                         Une  `Map`  qui associe les noms et instances des cookies. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Paramètres d’initialisation |         **initParam** | Une  `Map`  qui associe les données contenues dans les 
champs  `<param-name>`  et  `<param-value>`  de 
la section  `<init-param>`  du fichier web.xml. |
+-----------------------------+-----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+



La première chose à remarquer dans ce tableau, c'est que le seul objet implicite en commun entre les JSP et les expressions EL est le **pageContext**. Je ne m'attarde pas plus longtemps sur cet aspect, nous allons y revenir dans le chapitre suivant.

La seconde, c'est la différence flagrante avec les objets implicites JSP : tous les autres objets implicites de la technologie EL sont des  `Map`  !


[[question]]
| D'ailleurs, qu'est-ce que c'est que toute cette histoire de  `Map`  et d'associations entre des noms et des valeurs ?

Ça peut vous paraître compliqué, mais en réalité c'est très simple. C'est un outil incontournable en Java, et nous venons d'en manipuler une lorsque nous avons découvert les expressions EL. Mais si jamais vous ne vous souvenez pas bien des  [collections Java](http://www.siteduzero.com/tutoriel-3-10409-les-collections-d-objets.html) , sachez qu'une  [ `Map` ](http://docs.oracle.com/javase/6/docs/api/java/util/Map.html)  est un objet qui peut se représenter comme un tableau à deux colonnes :


- la première colonne contient ce que l'on nomme les **clés**, qui doivent obligatoirement être uniques ;
- la seconde contient les valeurs, qui peuvent quant à elles être associées à plusieurs clés.

Chaque ligne du tableau ne peut contenir qu'une clé et une valeur. Voici un exemple d'une  `Map<String, String>`  représentant une liste d'aliments et leurs types :


+-----------------+-----------------+
| Aliments (Clés) | Types (Valeurs) |
+=================+=================+
|           pomme |           fruit |
+-----------------+-----------------+
|         carotte |          légume |
+-----------------+-----------------+
|           boeuf |          viande |
+-----------------+-----------------+
|       aubergine |          légume |
+-----------------+-----------------+
|               … |               … |
+-----------------+-----------------+
Table:Fruits & légumes...

Vous voyez bien ici qu'un même type peut être associé à différents aliments, mais qu'un même aliment ne peut exister qu'une seule fois dans la liste. Eh bien c'est ça le principe d'une  `Map`  : c'est un ensemble d'éléments uniques auxquels on peut associer n'importe quelle valeur.


[[question]]
| Quel est le rapport avec la technologie EL ?

Le rapport, c'est que comme nous venons de le découvrir, nos expressions EL sont capables d'accéder au contenu d'une  `Map` , de la même manière qu'elles sont capables d'accéder aux propriétés d'un bean. En guise de rappel, continuons notre exemple avec la liste d'aliments, et créons une page **test_map.jsp**, dans laquelle nous allons implémenter rapidement cette  `Map`  d'aliments :


```jsp
<%@ page import="java.util.Map, java.util.HashMap" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Test des Maps et EL</title>
    </head>
    <body>
    <p>
       <%
         Map<String, String> aliments = new HashMap<String, String>();
         aliments.put( "pomme","fruit" );
         aliments.put( "carotte","légume" );
         aliments.put( "boeuf","viande" );
         aliments.put( "aubergine","légume" );
         request.setAttribute( "aliments", aliments );
       %>
       ${ aliments.pomme } <br /> <!-- affiche fruit -->
       ${ aliments.carotte } <br /> <!-- affiche légume -->
       ${ aliments.boeuf } <br /> <!-- affiche viande -->
       ${ aliments.aubergine } <br /><!-- affiche légume -->
    </p>
    </body>
</html>
```

J'utilise ici une scriptlet Java pour initialiser rapidement la  `Map`  et la placer dans un attribut de la requête nommé **aliments**. Ne prenez bien évidemment pas cette habitude, je ne procède ainsi que pour l'exemple et vous rappelle que nous cherchons à éliminer le code Java de nos pages JSP ! Rendez-vous alors sur  [http://localhost:8080/test/test_map.jsp](http://localhost:8080/test/test_map.jsp) , et observez le bon affichage des valeurs. Comme je vous l'ai annoncé un peu plus tôt, j'utilise la notation avec l'opérateur _point_ - ici dans les lignes 18 à 21 - pour accéder aux valeurs contenues dans la  `Map` , mais il est tout à fait possible d'utiliser la notation avec les crochets.


[[question]]
| D'accord, avec des expressions EL, nous pouvons accéder au contenu d'objets de type  `Map` . Mais ça, nous le savions déjà… Quel est le rapport avec les objets implicites EL ?

Le rapport, c'est que tous ces objets sont des  `Map` , et que par conséquent nous sommes capables d'y accéder depuis des expressions EL, de la même manière que nous venons de parcourir notre  `Map`  d'aliments ! Pour illustrer le principe, nous allons laisser tomber nos fruits et légumes et créer une page nommée **test_obj_impl.jsp**, encore et toujours à la racine de notre session, application, et y placer le code suivant :


```jsp
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Test des objets implicites EL</title>
    </head>
    <body>
    <p>
	<% 
	String paramLangue = request.getParameter("langue");
	out.println( "Langue : " + paramLangue );
	%>
	<br />
	<%
	String paramArticle = request.getParameter("article");
	out.println( "Article : " + paramArticle );
	%>
    </p>
    </body>
</html>
```

Vous reconnaissez aux lignes 10 et 15 la méthode  `request.getParameter()`  permettant de récupérer les paramètres transmis au serveur par le client à travers l'URL. Ainsi, il vous suffit de vous rendre sur  [http://localhost:8080/test/test_obj_impl.jsp?langue=fr&article=782](http://localhost:8080/test/test_obj_impl.jsp?langue=fr&article=782)  pour que votre navigateur vous affiche ceci (voir la figure suivante).


->![](http://uploads.siteduzero.com/files/383001_384000/383484.png)<-


Cherchez maintenant, dans le tableau fourni précédemment, l'objet implicite EL dédié à l'accès aux paramètres de requête… Trouvé ? Il s'agit de la  `Map`  nommée **param**. La technologie EL va ainsi vous mettre à disposition un objet dont le contenu peut, dans le cas de notre exemple, être représenté sous cette forme :


+------------------------+------------------------------+
| Nom du paramètre (Clé) | Valeur du paramètre (Valeur) |
+------------------------+------------------------------+
|                 langue |                           fr |
+------------------------+------------------------------+
|                article |                          782 |
+------------------------+------------------------------+


Si vous avez compris l'exemple avec les fruits et légumes, alors vous avez également compris comment accéder à nos paramètres de requêtes depuis des expressions EL, et vous êtes capables de réécrire notre précédente page d'exemple sans utiliser de code Java ! Éditez votre fichier **test_obj_impl.jsp** et remplacez le code précédent par le suivant :


```jsp
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Test des objets implicites EL</title>
    </head>
    <body>
    <p>
	Langue : ${ param.langue }
	<br />
	Article : ${ param.article }
    </p>
    </body>
</html>
```

Actualisez la page dans votre navigateur, et observez le même affichage que dans l'exemple précédent.
Pratique et élégant, n'est-ce pas ? :)


[[question]]
| D'accord, dans ce cas cela fonctionne bien : chaque paramètre a un nom unique, et est associé à une seule valeur quelconque. Mais qu'en est-il des lignes marquées avec ****** dans le tableau ? Est-il possible d'associer un unique paramètre à plusieurs valeurs à la fois ?

Oui, il est tout à fait possible d'associer une clé à des valeurs multiples. C'est d'ailleurs tout à fait logique, puisque derrière les rideaux il s'agit tout simplement d'objets de type  `Map`  ! L'unique différence entre les objets implicites **param** et **paramValues**, ainsi qu'entre **header** et **headerValues**, se situe au niveau de la nature de l'objet utilisé dans la  `Map`  et des valeurs qui y sont stockées :


- pour **param** et **header**, une seule valeur est associée à chaque nom de paramètre, via une  `Map<String,String>`  ;
- pour **paramValues** et **headerValues** par contre, ce sont plusieurs valeurs qui vont être associées à un même nom de paramètre, via une  `Map<String,String[]>` .


[[question]]
| Quand pouvons-nous rencontrer plusieurs valeurs pour un seul et même paramètre ?  o_O

Tout simplement en précisant plusieurs fois un paramètre d'URL avec des valeurs différentes ! Par exemple, accédez cette fois à la page de tests avec l'URL  [http://localhost:8080/test/test_obj_impl.jsp?langue=fr&article=782&langue=zh](http://localhost:8080/test/test_obj_impl.jsp?langue=fr&article=782&langue=zh) . Cette fois, la technologie EL va vous mettre à disposition un objet dont le contenu peut être représenté ainsi :


+------------------------+------------------------------+
| Nom du paramètre (Clé) | Valeur du paramètre (Valeur) |
+========================+==============================+
|                 langue |                      [fr,zh] |
+------------------------+------------------------------+
|                article |                          782 |
+------------------------+------------------------------+


La  `Map`  permettant d'accéder aux valeurs du paramètre **langue** n'est plus une  `Map<String,String>` , mais une  `Map<String,String[]>` . Si vous ne modifiez pas le code de l'expression EL dans votre page JSP, alors vous ne pourrez qu'afficher la première valeur du tableau des langues, retournée par défaut lorsque vous utilisez l'expression  `${param.langue}` . 

Afin d'afficher la seconde valeur, il faut cette fois non plus utiliser l'objet implicite **param**, mais utiliser l'objet implicite nommé **paramValues**. Remplacez à la ligne 9 de votre fichier **test_obj_impl.jsp** l'expression  `${param.langue}`  par l'expression  `${paramValues.langue[1]}` . Actualisez alors la page dans votre navigateur, et vous verrez alors s'afficher la valeur **zh** !


[[information]]
| Le principe est simple : alors qu'auparavant en écrivant  `${param.langue}`  vous accédiez directement à la  `String`  associée au paramètre **langue**, cette fois en écrivant  `${paramValues.langue}`  vous accédez non plus à une  `String` , mais à un tableau de  `String` . Voilà pourquoi il est nécessaire d'utiliser la notation avec crochets pour accéder aux différents éléments de ce tableau !


En l'occurrence, puisque seules deux langues ont été précisées dans l'URL, il n'existe que les éléments d'indices 0 et 1 dans le tableau, contenant les valeurs **fr** et **zh**. Si vous essayez d'accéder à un élément non défini, par exemple en écrivant  `${paramValues.langue[4]}` , alors l'expression EL détectera une valeur nulle et n'affichera rien. De même, vous devez **obligatoirement** cibler un des éléments du tableau ici. Si vous n'écrivez que  `${paramValues.langue}` , alors l'expression EL vous affichera la référence de l'objet Java contenant votre tableau…


Par ailleurs, sachez qu'il existe d'autres cas impliquant plusieurs valeurs pour un même paramètre. Prenons un exemple HTML très simple : un  `<select>`  à choix multiples !


```html
<form method="post" action="">
   <p>
       <label for="pays">Dans quel(s) pays avez-vous déjà voyagé ?</label><br />
       <select name="pays" id="pays" multiple="multiple">
           <option value="france">France</option>
           <option value="espagne">Espagne</option>
           <option value="italie">Italie</option>
           <option value="royaume-uni">Royaume-Uni</option>
           <option value="canada">Canada</option>
           <option value="etats-unis">Etats-Unis</option>
           <option value="chine" selected="selected">Chine</option>
           <option value="japon">Japon</option>
       </select>
   </p>
</form>
```

Alors que via un  `<select>`  classique, il n'est possible de choisir qu'une seule valeur dans la liste déroulante, dans cet exemple grâce à l'option  `multiple="multiple"` , il est tout à fait possible de sélectionner plusieurs valeurs pour le seul paramètre nommé **pays**. Eh bien dans ce genre de cas, l'utilisation de l'objet implicite **paramValues** est nécessaire également : c'est le seul moyen de récupérer la liste des valeurs associées au seul paramètre nommé **pays** ! 


Pour ce qui est de l'objet implicite **headerValues** par contre, sa réelle utilité est discutable. En effet, s'il est possible de définir plusieurs valeurs pour un seul paramètre d'un en-tête HTTP, celles-ci sont la plupart du temps séparées par de simples points-virgules et concaténées dans une seule et même  `String` , rendant l'emploi de cet objet implicite inutile. Bref, dans 99 % des cas, utiliser la simple  `Map`  **header** est suffisant. Ci-dessous un exemple d'en-têtes HTTP :


```HTTP
GET / HTTP/1.1
Host: www.google.fr
User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6 ( .NET CLR 3.5.30729)
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-us,en;q=0.5
Accept-Encoding: gzip,deflate
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive: 115
Connection: keep-alive
```

Vous remarquez bien dans cet exemple que chaque paramètre (**Host**, **User-Agent**, **Accept**, etc.) n'est défini qu'une seule fois, et que les valeurs sont simplement concaténées les unes à la suite des autres sur la même ligne.



[[information]]
| Voilà donc la solution à notre problème : les objets implicites EL sont des raccourcis qui rendent l'accès aux différentes portées et aux différents concepts liés à HTTP extrêmement pratiques !


Nous allons nous arrêter là pour les explications sur les objets implicites, l'important pour le moment est que vous compreniez bien leur mode de fonctionnement. Ne vous inquiétez pas si vous ne saisissez pas l'utilité de chacun d'entre eux, c'est tout à fait normal, certains concepts vous sont encore inconnus. La pratique vous fera prendre de l'aisance, et j'apporterai de plus amples explications au cas par cas dans les exemples de ce cours. Avant de passer à la suite, un petit avertissement quant au nommage de vos objets. 


[[attention]]
| Faites bien attention aux noms des objets implicites listés ci-dessus. Il est fortement déconseillé de déclarer une variable portant le même nom qu'un objet implicite, par exemple **param** ou **cookie**. En effet, ces noms sont déjà utilisés pour identifier des objets implicites, et cela pourrait causer des comportements plutôt inattendus dans vos pages et expressions EL. **Bref, ne cherchez pas les ennuis : ne donnez pas à vos variables un nom déjà utilisé par un objet implicite.**


Beaucoup de nouvelles notations vous ont été présentées, prenez le temps de bien comprendre les exemples illustrant l'utilisation des balises et des expressions. Lorsque vous vous sentez prêts, passez avec moi au chapitre suivant, et tentez alors de réécrire notre précédente page d'exemple JSP, en y faisant cette fois intervenir uniquement ce que nous venons d'apprendre !



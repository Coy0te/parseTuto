parseTuto
=========

Prototype de parseur zCode -> markdown.


# Technos

- développé en Java
- s'appuie sur les libs `com.bethecoder.ascii_table` (dessin de tableaux en ascii) et `org.jsoup` (parseur HTML)


# Utilisation

La seule classe est **TestJSoup.java.**, dans le package `zds.parseTuto`.
Elle prend en entrée un fichier texte formaté en zCode, et produit en sortie un fichier formaté en markdown.

Elle est commentée, il suffit de l'exécuter (sous Eclipse, Run As > Java App) pour générer le fichier en sortie.


# Bugs connus

Les tableaux ne sont pas entièrement gérés :
- un saut de ligne dans une cellule casse la mise en forme markdown du tableau
- les attributs de cellule `rowspan` et `colspan` ne sont pas bien gérés, pour le moment le contenu d'une cellule spannée est dupliqué dans sa version splitée.

A priori, pas d'autre bug.


# TODO

- Corriger les bugs (= réécrire la gestion actuelle des tableaux...)
- Greffer le tout dans une méthode qui :
    - prend en entrée un répertoire contenant tous les .tuto ;
    - lit chaque fichier
    - parse et modifie à la volée chaque section : `<introduction>`, `<conclusion>`, `<texte>`, `<label>`, `<reponse>` et `<explication>`.


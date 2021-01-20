-- MySQL dump 10.18  Distrib 10.3.27-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: signsatform
-- ------------------------------------------------------
-- Server version	10.3.27-MariaDB-0+deb10u1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `articles`
--

DROP TABLE IF EXISTS `articles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `articles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description_picture` varchar(255) DEFAULT NULL,
  `description_text` varchar(10000) DEFAULT NULL,
  `description_video` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articles`
--

LOCK TABLES `articles` WRITE;
/*!40000 ALTER TABLE `articles` DISABLE KEYS */;
INSERT INTO `articles` VALUES (1,'https://s2.dmcdn.net/v/SheCa1Vz8C9zLKlut/360x360','<h2>Article 1 – Objet</h2><p>Les présentes conditions générales d’utilisation (« CGU ») ont pour objet de définir les conditions et les modalités selon lesquelles Orange, société anonyme au capital social de 10 640 226 396 €, immatriculée au RCS de Paris sous le numéro 380 129 866, dont le siège social est situé au 78 rue Olivier de Serres, 75015 Paris, France (ci-après dénommée « Orange »), met le service Signs@Work (ci-après le « Service ») à la disposition des Utilisateurs et Contributeurs (tel que défini ci-dessous). Ce Service est actuellement proposé dans une version de test.</p><p>L’Utilisateur et le Contributeur déclarent avoir pris connaissance et accepté les présentes CGU en cochant la case « Acceptation » et utilisant le Service.</p><p>Orange se réserve le droit de modifier ou de faire évoluer ce service ainsi que les CGU. Ces modifications et évolutions entreront en vigueur dès leur mise en ligne.</p><p>Dans le cas d’une modification des CGU l’Utilisateur et/ou Contributeur devra accepter les nouvelles CGU afin de continuer à utiliser le service. Orange se réserve enfin le droit de cesser, après préavis, la fourniture du service de manière totale ou partielle à tout moment.</p>','https://www.dailymotion.com/embed/video/k4rL9TWyMKt5hbwz3Ok','fr','Article 1 – Objet','Cgu'),(2,'https://s1.dmcdn.net/v/SheFT1Vz8HL5ZdHGG/360x360','<h2>Article 2 – Définitions</h2><p>«<b> Contenu </b>» : désigne toutes les informations présentées par un Contributeur sur Signs@Work telles que les données, textes, graphismes, images, sons, vidéos, logos, symboles, code html.</p><p>«<b> Utilisateur </b>» : désigne toute personne physique qui fait usage du Service Signs@Work à des fins de consultation ou de recherche et d’utilisation du Contenu.</p><p>«<b> Contributeur </b>» : désigne toute personne physique qui fait usage du Service Signs@Work aux fins de publier du Contenu et de l’enrichir son Contenu.</p>','https://www.dailymotion.com/embed/video/k6LbwzND1idoo0wz3Rj','fr','Article 2 – Définitions','Cgu'),(3,'https://s1.dmcdn.net/v/Shjn31VzMpctof7M5/360x360','<h2>Article 3 - Description du service Signs@Work et conditions d’inscription</h2><p>Signs@Work est un service web destiné à construire et à utiliser en mode collaboratif un dictionnaire en Langue des Signes Française (LSF) des jargons métiers et entreprises.</p><p>Il s’adresse d’abord aux salariés s’exprimant en Langue des Signes Française et à leurs interprètes.</p><p>Les fonctions principales sont de proposer un signe sous forme d’une courte vidéo, de faire une demande de signe, de rechercher, consulter et commenter des signes. Mais aussi de créer ses listes de signes et de les partager.</p><p>Les contenus proposés sont hébergés sur une chaine privée Dailymotion et ne sont visibles que via le Service Signs@Work.</p><p>Signs@Work est proposé à titre gratuit, accessible notamment à l’adresse : <a href=\"https://signsatwork.orange-labs.fr\">https://signsatwork.orange-labs.fr</a>. L’Utilisateur et le Contributeur devront disposer du matériel requis pour l’utilisation de Signs@Work.</p><p>La consultation du Service est ouverte à toute personne physique majeure de plus de 18 ans souhaitant utiliser le service à des fins non-commerciales.</p>\n<p>Pour pouvoir utiliser le Service Signs@Work, l’Utilisateur (et/ou Contributeur) devra s’inscrire et créer un compte.</p><p>Lors de l’ouverture du compte, l’Utilisateur (et/ou Contributeur) devra communiquer des informations personnelles demandées relatives à son identité telles que nom, prénom, adresse email, A défaut, l’ouverture du compte Utilisateur ne sera pas possible.</p><p>L’Utilisateur (et/ou Contributeur) s’engage à ce que les informations communiquées lors de son inscription soient fiables et conformes à la réalité et mises à jour en permanence.</p><p>Dans l’hypothèse où l’Utilisateur (et/ou Contributeur) fournirait des informations fausses, inexactes, périmées ou incomplètes, Orange, dès lors que ses services en ont connaissance, est en droit de résilier son compte sans délai.</p><p>Tout Utilisateur (et/ou Contributeur)  ne remplissant pas les conditions prévues dans les CGU se verra interdire l’accès et l’utilisation du Service. Orange se réserve ainsi le droit de suspendre temporairement ou définitivement le compte de tout Utilisateur (et/ou Contributeur).</p><p>L’Utilisateur (et/ou Contributeur) reconnait et accepte de garder confidentiel le login et le mot de passe permettant un accès à son compte. Le login et le mot de passe sont personnels et ne doivent pas être partagés ni transférés à une quelconque personne physique ou morale, sous quelque forme que ce soit.</p><p>L’Utilisateur (et/ou Contributeur)  est seul responsable de son compte et de l’utilisation qu’il en fait et devra informer Orange en cas d’usage non autorisé de son compte ou de toute autre atteinte à la sécurité de celui-ci.</p><p>Orange fournira ses meilleurs efforts afin d’assurer la meilleure disponibilité et qualité possible du Service. Orange s’efforcera de maintenir accessible le service 7 jours sur 7 et 24 heures sur 24.</p><p>Orange se réserve toutefois, notamment pour des raisons de maintenance, le droit de suspendre momentanément et sans préavis l’accès au service, sans que cette interruption puisse ouvrir droit à une quelconque indemnité au bénéfice de l’Utilisateur, le Contributeur ou de tout tiers.</p><p>Orange s’efforce d’offrir aux Utilisateurs et Contributeurs l’information la plus fiable et qualitative qui soit. Orange ne garantit cependant pas l’exactitude et l’exhaustivité de l’information diffusée sur Signs@Work.</p><p>Les usages publicitaires et commerciaux sont proscrits du service Signs@Work.</p><p>En utilisant le Service, l’Utilisateur ou le Contributeur déclarent et garantissent qu’ils sont parfaitement informés des caractéristiques et des contraintes d’Internet, notamment que les transmissions de données et d’informations sur Internet ne bénéficient que d’une fiabilité technique relative. Aussi les informations circulant sur Internet ne sont pas protégées contre des détournements ou contre des virus éventuels.</p><p>La responsabilité d’Orange ne saurait être engagée notamment dans les cas suivants :</p><p>- contamination par virus ou tout autre élément à risque du service et des équipements de l’Utilisateur ou Contributeur, ou autre intrusion malveillante de tiers malgré les mesures raisonnables de sécurité mises en place par l’Utilisateur, le Contributeur et par Orange,</p><p>- difficultés d’accès, de fonctionnement, interruption du Service Signs@Work,</p><p>- mauvaise utilisation du Service par l’Utilisateur ou le Contributeur,</p><p>- inadéquation entre le Service et les attentes de l’Utilisateur ou le Contributeur.</p><p>L’Utilisateur et le Contributeur sont seuls responsables de l’usage fait du droit d’utilisation qui leur est accordé au terme des présentes et garantissent Orange contre toute utilisation illicite, non conforme et/ou non autorisée du Service Signs@Work.</p><p>L’Utilisateur et le Contributeur sont les  seuls responsables dans ces conditions des dommages et préjudices directs causés à Orange ou à tout tiers du fait de l’utilisation du Service.</p><p>Orange ne saurait donc être tenu pour responsable des dommages directs ou indirects, subis par les Utilisateurs, Contributeurs  ou par des tiers qui trouveraient leur source dans l’information diffusée sur Signs@Work, et de manière plus générale, dans la consultation et l’utilisation de Signs@Work.</p>','https://www.dailymotion.com/embed/video/k3x5wwzhT3vsiewz9Kj','fr','Article 3 - Description du service Signs@Work et conditions d’inscription','Cgu'),(4,'https://s2.dmcdn.net/v/Sheal1Vz8vwz4C_lc/360x360','<h2>Article 4 – Publication du Contenu par le Contributeur</h2><p>En fournissant un Contenu sur Signs@Work, chaque Contributeur est responsable du Contenu qu’il publie sur le Service Signs@Work.</p><p>En conséquence, chaque Contributeur est tenu au respect des dispositions légales et réglementaires en vigueur.</p><p>Il lui appartient en conséquence de s’assurer que le stockage et la diffusion de ce Contenu via Signs@Work ne constituent pas une violation des droits de tiers, et notamment aux droits de propriété intellectuelle de tiers ou droit à l’image (notamment Contenus protégés que le Contributeur n’aurait pas réalisés personnellement ou pour lesquels il ne dispose pas des autorisations nécessaires).</p><p>La diffusion, reproduction ou la représentation des Contenus publiés sur Signs@Work doit s’effectuer dans le respect du code de la propriété intellectuelle, notamment des règles protectrices des droits d’auteur et du droit à l’image.</p><p>Toute réutilisation autorisée doit porter la mention explicite du nom de l’auteur et/ou de la source dont l’œuvre est tirée.</p><p>En mettant en ligne et en mettant à la disposition des Utilisateurs un Contenu sur et/ou à travers Signs@Work, le Contributeur garantit qu’il détient tous les droits et autorisations nécessaires de la part des titulaires légitimes de droit sur les Contenus concernés.</p><p>A défaut, s’il apparaît d’évidence que le Contenu est intégré sans droit sur Signs@Work, il pourra être immédiatement retiré et/ou le Contributeur responsable de cette intégration sur Signs@Work pourra être interdit d’utiliser le Service sans formalité préalable. De plus, tout contrevenant encourt, à titre personnel, les sanctions pénales et autres spécifiques au Contenu litigieux (peines d’emprisonnement et amende), outre des éventuels dommages et intérêts.</p><p>Ces Contenus ne doivent par ailleurs en aucun cas constituer une atteinte aux personnes (notamment diffamation, insultes, injures, etc.), au respect de la vie privée, à l’ordre public ou aux bonnes mœurs (notamment, apologie des crimes contre l’humanité, incitation à la violence ou à la haine contre tout individu ou groupe, pornographie enfantine, etc.).</p><p>Enfin, l’Utilisateur et le Contributeur s’engagent  à ne pas déposer un Contenu ou autre donnée, programme malveillant qui serait susceptible de contenir des virus, chevaux de Troie, destinés à endommager ou interférer le bon fonctionnement du Service.</p>','https://www.dailymotion.com/embed/video/k68s5eNbCE2PIpwz4dh','fr','Article 4 – Publication du Contenu par le Contributeur','Cgu'),(5,'https://s2.dmcdn.net/v/Sheh01Vz90xK9Wppi/360x360','<h2>Article 5 – Droits concédés sur le Contenu et conditions d’utilisation</h2><p>Chaque Contributeur est et reste détenteur des droits associés sur le Contenu qu’il choisit de publier.</p><p>Lorsqu’un Contributeur soumet un Contenu sur Signs@Work, celui-ci concède à Orange, le droit non exclusif, cessible (y compris le droit de sous-licencier), à titre gracieux, et pour le monde entier, d’utiliser, de reproduire, de distribuer, de réaliser des œuvres dérivées, de représenter et d’exécuter le Contenu dans le cadre de l’exploitation du service Signs@Work ou en relation avec la mise à disposition de Contenu sur Signs@Work, notamment, sans limitation, pour la promotion et la redistribution de tout ou partie du Contenu (et des œuvres dérivées qui en résultent), en tout format, sur tout support et via tous les canaux média.</p><p>Ainsi le Contributeur concède aussi à Orange le droit de masquer ou de supprimer un Contenu dans le cadre des processus associés au fonctionnement du service, tels que : modération, animation, validation, péremption.</p>','https://www.dailymotion.com/embed/video/k2356zMIHLuwPTwz4jK','fr','Article 5 – Droits concédés sur le Contenu et conditions d’utilisation','Cgu'),(6,'https://s1.dmcdn.net/v/ShlOP1VzQrg5oeUtq/360x360','<h2>Article 6 – Propriété intellectuelle et droit d’utilisation du service Signs@Work</h2><p>L’Utilisateur ou le Contributeur reconnaît qu’il n’acquiert aucun droit de propriété intellectuelle sur les éléments figurant sur le service Signs@Work ou le service en lui-même, et appartenant à Orange.</p><p>L’Utilisateur et le Contributeur ne peuvent déduire de l’utilisation du Service Signs@Work une quelconque autorisation d’utiliser les marques qui demeurent la propriété d’Orange, pour quelque besoin que ce soit.</p><p>Le code source du service Signs@Work est disponible selon les termes de la licence open source GNU GPL version 2.0 (https://github.com/Orange-OpenSource/signs-at-work-social-network-dictionnary#readme).</p><p>Ainsi l’Utilisateur peut l’utiliser dans le respect des termes de cette licence et sous réserve de se conformer aux présentes CGU.</p>','https://www.dailymotion.com/embed/video/k3eJUL6Ilkqe4rwzbsZ','fr','Article 6 – Propriété intellectuelle et droit d’utilisation du service Signs@Work','Cgu'),(7,'https://s1.dmcdn.net/v/Shlc11VzRPWOeks_N/360x360','<h2>Article 7  Données Personnelles</h2><p>L’utilisation du Service Signs@Work nécessite, de la part de l’Utilisateur (et/ou Contributeur), la communication d’un certain nombre de données à caractère personnel (les « Données Personnelles ») qui seront traitées par Orange dans le cadre de l’exécution des présentes CGU conformément à la Politique de données personnelles du Service Sign@work qui fait partie intégrante de ces CGU.</p>','https://www.dailymotion.com/embed/video/k6WZaiVaa6Ya6mwzbH3','fr','Article 7  Données Personnelles','Cgu'),(8,'https://s1.dmcdn.net/v/ShjyP1VzNHJUaNY8t/360x360','<h2>Article 8- Résiliation, Suspension et fermeture de l’accès au service Signs@Work</h2><p>Orange, dans la mesure où il en aura la capacité technique, se réserve le droit de supprimer de plein droit et/ou de suspendre l’accès au Service, sans préavis ni indemnité, en cas de manquement de l’Utilisateur ou Contributeur à l’une quelconque des obligations souscrites au terme des présentes, notamment à la suite d’une notification que l’Utilisateur ou Contributeur fait un usage du Service contraire à la loi, aux bonnes mœurs  ou à l’ordre public ou de nature à porter préjudice aux tiers.</p>','https://www.dailymotion.com/embed/video/k5SPxCvYMXa0BKwz9W1','fr','Article 8- Résiliation, Suspension et fermeture de l’accès au service Signs@Work','Cgu'),(9,'https://s2.dmcdn.net/v/Shk4e1VzNcRBazEEi/360x360','<h2>Article 9 - Réclamations</h2><p>Toute réclamation ou notification doit être adressée par courriel à l’adresse de messagerie suivante : <a href=\"mailto:signsatwork.support@orange.com\">signsatwork.support@orange.com</a>.</p>','https://www.dailymotion.com/embed/video/k518me6y5JEeoswza4w','fr','Article 9 - Réclamations','Cgu'),(10,'https://s1.dmcdn.net/v/ShkDN1VzNwbLslv2P/360x360','<h2>Article 10 – Loi applicable et juridiction compétente</h2><p>Ces CGU sont soumis au droit français. Tout litige non réglé de manière transactionnelle sera porté devant le tribunal compétent de Paris.</p>','https://www.dailymotion.com/embed/video/k24bnt8YasU80Qwzadx','fr','Article 10 – Loi applicable et juridiction compétente','Cgu'),(11,'https://s2.dmcdn.net/v/Shl6_1VzQBER3-aP9/360x360','<h2>Article 11 - Disposition Diverses </h2><p>Dans l’éventualité où l’une quelconque des stipulations de ces CGU serait déclarée nulle ou sans effet, les stipulations restantes seront considérées comme applicables de plein droit.</p><p>Les stipulations déclarées nulles et non valides seront alors remplacées par des stipulations qui se rapprocheront le plus quant à leur contenu des stipulations initialement arrêtées.</p><p>Les parties ne seront pas tenues pour responsables, ou considérées comme ayant failli aux présentes CGU, pour tout retard ou inexécution, lorsque la cause du retard ou de l’inexécution est liée à un cas de force majeure telle que définie par la jurisprudence des tribunaux français.</p>','https://www.dailymotion.com/embed/video/k6RBZ1YZIXslbWwzbb1','fr','Article 11 - Disposition Diverses','Cgu'),(12,'https://s1.dmcdn.net/v/Shlc11VzRPWOeks_N/360x360','<h2>Politique de Données Personnelles</h2><p>L’utilisation du service Signs@Work nécessite, de la part de l’Utilisateur et/ou Contributeur tels que définis dans les Conditions Générales d’Utilisation du service Signs@Work, la communication d’un certain nombre de données à caractère personnel (les « Données Personnelles ») qui seront traitées par Orange dans le cadre de la fourniture du service et détaillées dans cette Politique de Données Personnelles.</p><p>L’Utilisateur et/ou Contributeur est seul responsable des Données Personnelles qu’il communique à Orange au travers du service Signs@Work et déclare que les Données Personnelles fournies sont parfaitement renseignées et exactes.</p>','https://www.dailymotion.com/embed/video/k6WZaiVaa6Ya6mwzbH3','fr','Politique de Données Personnelles','PersonalData'),(13,'https://s2.dmcdn.net/v/ShkcN1VzP0YJTMth_/360x360','<h2>1 - Nature des Données Personnelles traitées</h2><p>Les Données Personnelles traitées par Orange dans le cadre du service Signs@Work sont les données qui sont communiquées volontairement par l’Utilisateur (et/ou Contributeur) lors de son inscription au service :</p><p>Données d’identification : nom, prénom.</p><p>Données de contact : adresse e-mail.</p><p>Données de connexion, d’usage des services, d’interaction et de connexion.</p><p>Données de contenu/Content data : commentaires, votes dans le service, vidéos en Langue des Signes</p><p>Caractéristiques personnelles : pour le Contributeur  uniquement, visage et haut du corps de l’utilisateur sur la vidéo.</p>','https://www.dailymotion.com/embed/video/k7HSmRcGJzFBdzwzaDl','fr','1 - Nature des Données Personnelles traitées','PersonalData'),(14,'https://s1.dmcdn.net/v/Shm3E1VzRxyxjougb/360x360','<h2>2 - Finalités des traitements</h2><p>Les Données Personnelles traitées par Orange dans le cadre du service Signs@Work sont les données qui sont communiquées volontairement par l’Utilisateur (et/ou Contributeur) lors de son inscription au service :</p><p>L’Utilisateur et/ou Contributeur est informé de ce que les Données Personnelles signalées comme étant obligatoires sur le formulaire d’inscription et communiquées volontairement par l’Utilisateur et/ou Contributeur sont nécessaires pour l’utilisation du service Signs@Work.</p><p>Le traitement de l’ensemble des Données Personnelles collectées dans le cadre du service Signs@Work permet à Orange de :</p><p>- garantir l’accès et l’utilisation au service;</p><p>- améliorer l’utilisation du service;</p><p>- vérification, identification et authentification des données transmises par l’Utilisateur (et/ou Contributeur).</p>','https://www.dailymotion.com/embed/video/k1zMWGUK5VbWVDwzcbc','fr','2 - Finalités des traitements','PersonalData'),(15,'https://s2.dmcdn.net/v/ShmIQ1VzSPvSC2Ywq/360x360','<h2>3 - Destinataires des Données Personnelles</h2><p>Les Données Personnelles collectées dans le cadre du service sont destinées aux équipes d’Orange.</p><p>Les Données Personnelles pourront également être traitées par des partenaires ou sous-traitant d’Orange pour la fourniture de certaines prestations.</p><p>A cet effet, l’Utilisateur et/ou Contributeur donne son accord pour le traitement de ses Données Personnelles par un partenaires ou sous-traitant d’Orange aux finalités susvisées.</p><p>Orange prend les dispositions nécessaires avec ses partenaires et sous-traitants pour garantir un niveau de protection de vos données adéquat et ce en toute conformité avec la règlementation applicable.</p><p>L’Utilisateur et/ou Contributeur est informé que ses Données Personnelles peuvent être éventuellement divulguées en application d’une loi, d’un règlement ou en vertu d’une décision d’une autorité réglementaire ou judiciaire compétente.</p>','https://www.dailymotion.com/embed/video/k6spzohLHE3RDQwzcqS','fr','3 - Destinataires des Données Personnelles','PersonalData'),(16,'https://s1.dmcdn.net/v/Shmn31VzSmBRmUmTr/360x360','<h2>4 - Conservation des Données Personnelles</h2><p>Les Données Personnelles des Utilisateurs et/ou Contributeurs sont stockées chez Orange et sont conservées et archivées confidentiellement pour la durée strictement nécessaire à la réalisation des finalités susvisées.</p><p>Les données peuvent être conservées plus longtemps pour tenir compte du respect des obligations légales et réglementaires auxquelles Orange est soumise.</p><p>L’Utilisateur et/ou Contributeur est informé qu’en cas d’inactivité de son compte supérieure à trois ans ses Données Personnelles sont automatiquement supprimées.</p>','https://www.dailymotion.com/embed/video/kVWplUM1bDEmHWwzcWv','fr','4 - Conservation des Données Personnelles','PersonalData'),(17,'https://s1.dmcdn.net/v/Shn4u1VzTA7Wy3QNn/360x360','<h2>5 - Droits des Utilisateurs et/ou Contributeurs</h2><p>L’Utilisateur et/ou Contributeur dispose d’un droit d’accès, de rectification ou de suppression des Données Personnelles le concernant, ainsi que d’un droit d’opposition pour motif légitime sous réserve de justifier de son identité (indiquer son nom, prénom, adresse, numéro de téléphone et joindre un justificatif d’identité).</p><p>Il dispose également d’un droit à la limitation des traitements, ainsi que du droit à la portabilité de ses données.</p><p>L’Utilisateur et/ou Contributeur peut émettre des directives sur la conservation, la suppression ou la communication de ses Données Personnelles après son décès L’Utilisateurs et/ou Contributeurs peut exercer ses droits en écrivant à l’adresse suivante : <a href=\"mailto:signsatwork.support@orange.com\">signsatwork.support@orange.com</a></p>','https://www.dailymotion.com/embed/video/k7hbROOXocmoHDwzdgY','fr','5 - Droits des Utilisateurs et/ou Contributeurs','PersonalData');
/*!40000 ALTER TABLE `articles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-01-06 16:16:32
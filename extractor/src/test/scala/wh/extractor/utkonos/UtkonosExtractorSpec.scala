package wh.extractor.utkonos

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.{Category, ExtractedEntry}

class UtkonosExtractorSpec extends FlatSpec with Matchers {
  val rootCategory = Category("Продукты питания",null)
  val firstCategory = Category("Молочные продукты, мороженое", rootCategory)
  val secondCategory = Category("Молоко, сливки, молочные коктейли", firstCategory)
  "Utkonos extractor" should "return right list of entries" in {
    val extractor = new UtkonosExtractor
    val page = getClass.getClassLoader.getResource("utkonos/1.html")
    val root = page.toURI.resolve("./").toString
    extractor.extract(page).toList should be (
      List(
        ExtractedEntry("Utkonos", "Йогурт Valio питьевой Черешня 0,4%, 0,33мл", 5250, rootCategory, new URL(root + "images/photo/3248/3248738B.jpg?1418305614")),
        ExtractedEntry("Utkonos", "Вода СЕРЯБЬ Селивановская минеральная питьевая столовая газированная 0,33л пэт", 4700, rootCategory, new URL(root + "images/photo/3251/3251273B.jpg?1419421281")),
        ExtractedEntry("Utkonos", "Вода СЕРЯБЬ Селивановская минеральная питьевая столовая газированная 0,5л пэт", 5260, rootCategory, new URL(root + "images/photo/3251/3251274B.jpg?1419420753")),
        ExtractedEntry("Utkonos", "Вода СЕРЯБЬ Селивановская минеральная питьевая столовая негазированная 0,33л стекло", 7080, rootCategory, new URL(root + "images/photo/3251/3251272B.jpg?1419421985")),
        ExtractedEntry("Utkonos", "Вода СЕРЯБЬ Селивановская минеральная питьевая столовая негазированная 0,5л пэт спорт", 5670, rootCategory, new URL(root + "images/photo/3251/3251240B.jpg?1421402583")),
        ExtractedEntry("Utkonos", "Вода СЕРЯБЬ Селивановская минеральная питьевая столовая негазированная 0,75л пэт спорт", 6050, rootCategory, new URL(root + "images/photo/3251/3251271B.jpg?1419426305")),
        ExtractedEntry("Utkonos", "Мука Molini Pizzuti пшеничная \"Per Dolci\" для кондитерских изделий, 1кг", 13700, rootCategory, new URL(root + "images/photo/3250/3250885B.jpg?1418826035")),
        ExtractedEntry("Utkonos", "Мука Molini Pizzuti пшеничная \"Per Pasne\" для хлеба, 1кг", 13700, rootCategory, new URL(root + "images/photo/3250/3250884B.jpg?1418749127")),
        ExtractedEntry("Utkonos", "Мука Molini Pizzuti пшеничная \"Per Pasta\", 1кг", 15500, rootCategory, new URL(root + "images/photo/3250/3250709B.jpg?1418663627")),
        ExtractedEntry("Utkonos", "Напиток KОХХ Закат Европы тонизирующий безалкогольный газированный, 250мл", 8500, rootCategory, new URL(root + "images/photo/3251/3251291B.jpg?1419419816")),
        ExtractedEntry("Utkonos", "Напиток KОХХ Северная аврора тонизирующий безалкогольный газированный, 250мл", 8500, rootCategory, new URL(root + "images/photo/3251/3251279B.jpg?1419420150")),
        ExtractedEntry("Utkonos", "Напиток Ochakovo Джин & Тоник слабоалкогольный газированный 9%, 0,5л ж/б", 8480, rootCategory, new URL(root + "images/photo/3251/3251222B.jpg?1419060488")),
        ExtractedEntry("Utkonos", "Напиток Ochakovo Джин-Грейпфрут слабоалкогольный газированный 9%, 0,5л ж/б", 8480, rootCategory, new URL(root + "images/photo/3251/3251223B.jpg?1419060476")),
        ExtractedEntry("Utkonos", "Напиток Ochakovo Мохито Клубничный слабоалкогольный газированный 7,2%, 0,5л", 7830, rootCategory, new URL(root + "images/photo/3251/3251219B.jpg?1419060521")),
        ExtractedEntry("Utkonos", "Напиток Ochakovo Сидор слабоалкогольный газированный 9%, 1л", 15000, rootCategory, new URL(root + "images/photo/3251/3251225B.jpg?1419060249")),
        ExtractedEntry("Utkonos", "Напиток Ярис Ярый тонизирующий безалкогольный негазированный с натуральным соком растительными экстрактами 200мл стекл", 5410, rootCategory, new URL(root + "images/photo/3251/3251278B.jpg?1419417709")),
        ExtractedEntry("Utkonos", "Напиток Ярис Ярый тонизирующий безалкогольный негазированный с натуральным соком растительными экстрактами 250мл ПЭТ", 5410, rootCategory, new URL(root + "images/photo/3251/3251277B.jpg?1419418201")),
        ExtractedEntry("Utkonos", "Напиток Ярис Ясный тонизирующий безалкогольный негазированный с натуральным соком растительными экстрактами 200мл стек", 5410, rootCategory, new URL(root + "images/photo/3251/3251276B.jpg?1419417879")),
        ExtractedEntry("Utkonos", "Напиток Ярис Ясный тонизирующий безалкогольный негазированный с натуральным соком растительными экстрактами 250мл ПЭТ", 5410, rootCategory, new URL(root + "images/photo/3251/3251275B.jpg?1419418063")),
        ExtractedEntry("Utkonos", "Рис Campanini Nero Venere среднего размера черный, 1кг", 50100, rootCategory, new URL(root + "images/photo/3250/3250706B.jpg?1418662949")),
        ExtractedEntry("Utkonos", "Шоколад Kinder молочный Макси, 4*21г", 5890, rootCategory, new URL(root + "images/photo/3251/3251232B.jpg?1419256131")),
        ExtractedEntry("Utkonos", "Молоко Агуша стерилизованное с витаминами А и С 3,2%, 0,5л", 2720, firstCategory, new URL(root + "images/photo/3142/3142882B.jpg?1404369996")),
        ExtractedEntry("Utkonos", "Десерт творожный Чудо творожок воздушный Вишня-Черешня 4%,100г", 1690, firstCategory, new URL(root + "images/photo/3050/3050666B.jpg?1344529301")),
        ExtractedEntry("Utkonos", "Десерт творожный Чудо творожок воздушный Клубника-Земляника 4%,115г", 1690, firstCategory, new URL(root + "images/photo/3050/3050667B.jpg?1344529948")),
        ExtractedEntry("Utkonos", "Сливки Чистый Край 10%, 0,5л", 17400, secondCategory, new URL(root + "images/photo/3251/3251106B.jpg?1418970791")),
        ExtractedEntry("Utkonos", "Молоко Чистый Край отборное пастеризованное 3,4-4,2%, 1л", 10100, secondCategory, new URL(root + "images/photo/3251/3251103B.jpg?1418970700")),
        ExtractedEntry("Utkonos", "Молоко Караваево Деревенское пастеризованное 1,5%, 930г", 8450, secondCategory, new URL(root + "images/photo/3249/3249096B.jpg?1417508900")),
        ExtractedEntry("Utkonos", "Молоко Караваево Отборное пастеризованное 3,4-6%, 750г", 7260, secondCategory, new URL(root + "images/photo/3249/3249095B.jpg?1417539139")),
        ExtractedEntry("Utkonos", "Молоко Караваево топленое деревенское 3,2%, 450г", 4690, secondCategory, new URL(root + "images/photo/3249/3249103B.jpg?1417455119")),
        ExtractedEntry("Utkonos", "Молоко Молочная культура пастеризованное 3,5-4,5%, 500г", 7680, secondCategory, new URL(root + "images/photo/3229/3229697B.jpg?1398338253")),
        ExtractedEntry("Utkonos", "Коктейль Новая деревня молочный шоколадный 2,5%, 1л", 9020, secondCategory, new URL(root + "images/photo/3225/3225047B.jpg?1393397317")),
        ExtractedEntry("Utkonos", "Молоко Parmalat ультрапастеризованное 3,5%, 12*1л", 80300, secondCategory, new URL(root + "images/photo/3226/3226516B.jpg?1395741952")),
        ExtractedEntry("Utkonos", "Молоко Рузское пастеризованное 1,5%, 1000г", 11100, secondCategory, new URL(root + "images/photo/3229/3229949B.jpg?1398412787")),
        ExtractedEntry("Utkonos", "Напиток Актуаль Грейпфрут сыворотка с соком, 930г", 7280, secondCategory, new URL(root + "images/photo/3228/3228740B.jpg?1397544410")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 3,2%, 950г", 6700, secondCategory, new URL(root + "images/photo/3049/3049009B.jpg?1370326584")),
        ExtractedEntry("Utkonos", "Молоко Ясный луг ультрапастеризованное 3,2%, 1л", 4880, secondCategory, new URL(root + "images/photo/3118/3118959B.jpg?1365431309")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино отборное пастеризованное 3,4-4,5%, 0,93л", 5560, secondCategory, new URL(root + "images/photo/3052/3052548B.jpg?1342714180")),
        ExtractedEntry("Utkonos", "Молоко М Лианозовское ультрапастеризованное 3,2%, 950г", 5180, secondCategory, new URL(root + "images/photo/3074/3074902B.jpg?1337770127")),
        ExtractedEntry("Utkonos", "Сливки Домик в деревне 10% стерилизованные, 480г", 7470, secondCategory, new URL(root + "images/photo/3051/3051861B.jpg?1342618109")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне 1,5%, 950г", 6100, secondCategory, new URL(root + "images/photo/3051/3051864B.jpg?1340870487")),
        ExtractedEntry("Utkonos", "Молоко Утренней дойки отборное пастеризованное 3,4-6%, 1л", 8320, secondCategory, new URL(root + "images/photo/3225/3225064B.jpg?1393396776")),
        ExtractedEntry("Utkonos", "Молоко Нашей дойки пастеризованное 3,4-6%, 1л", 6480, secondCategory, new URL(root + "images/photo/3225/3225063B.jpg?1393396416")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино ультрапастеризованное 2,5%, 0,95л", 5900, secondCategory, new URL(root + "images/photo/3051/3051964B.jpg?1417590515")),
        ExtractedEntry("Utkonos", "Молоко Агуша стерилизованное с витаминами А и С 3,2%, 0,5л", 2720, secondCategory, new URL(root + "images/photo/3142/3142882B.jpg?1404369996")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино ультрапастеризованное 3,2%, 0,95л", 6800, secondCategory, new URL(root + "images/photo/3051/3051965B.jpg?1343155982")),
        ExtractedEntry("Utkonos", "Молоко Parmalat ультрапастеризованное 3,5%, 1л", 7260, secondCategory, new URL(root + "images/photo/3109/3109131B.jpg?1398335075")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 0,5%, 950г", 5900, secondCategory, new URL(root + "images/photo/3049/3049006B.jpg?1340871557")),
        ExtractedEntry("Utkonos", "Молоко Агуша с витаминами \"А\" и \"С\" с 8 месяцев 2,5%, 0,2л", 2210, secondCategory, new URL(root + "images/photo/3108/3108540B.jpg?1366373754")),
        ExtractedEntry("Utkonos", "Сливки Домик в деревне питьевые стерилизованные 10%, 200г", 4300, secondCategory, new URL(root + "images/photo/3049/3049026B.jpg?1337619548")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино пастеризованное 2,5%, 0,93л", 5980, secondCategory, new URL(root + "images/photo/3052/3052547B.jpg?1342713423")),
        ExtractedEntry("Utkonos", "Молоко Рузское пастеризованное 3,2%-4,0%, 1000г", 10900, secondCategory, new URL(root + "images/photo/3141/3141534B.jpg?1418994000")),
        ExtractedEntry("Utkonos", "Молоко Parmalat ультрапастеризованное 0,5%, 1л", 5920, secondCategory, new URL(root + "images/photo/3109/3109136B.jpg?1398335057")),
        ExtractedEntry("Utkonos", "Молоко Parmalat ультрапастеризованное 1,8%, 1л", 6530, secondCategory, new URL(root + "images/photo/3109/3109126B.jpg?1398335091")),
        ExtractedEntry("Utkonos", "Молоко Тёма питьевое ультрапастеризованное с кальцием 3,2%, 0,5л", 4060, secondCategory, new URL(root + "images/photo/3045/3045823B.jpg?1386255791")),
        ExtractedEntry("Utkonos", "Сливки Домик в деревне 20%, 200г", 7180, secondCategory, new URL(root + "images/photo/3049/3049025B.jpg?1337876707")),
        ExtractedEntry("Utkonos", "Молоко Останкинское пастеризованное 36 копеек 3,2%, 900г", 5340, secondCategory, new URL(root + "images/photo/3080/3080210B.jpg?1398331225")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 2,5%, 950г", 6860, secondCategory, new URL(root + "images/photo/3055/3055875B.jpg?1349858917")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 3,2%, 500г", 4330, secondCategory, new URL(root + "images/photo/3222/3222974B.jpg?1391159347")),
        ExtractedEntry("Utkonos", "Молоко Новая деревня отборное пастеризованное 3,5%, 1л", 8540, secondCategory, new URL(root + "images/photo/3143/3143577B.jpg?1401534747")),
        ExtractedEntry("Utkonos", "Молоко Агуша стерилизованное детское с пребиотиком для детей с 12 месяцев 2,5%, 0,2л", 2790, secondCategory, new URL(root + "images/photo/3108/3108535B.jpg?1369725655")),
        ExtractedEntry("Utkonos", "Сливки Домик в деревне стерилизованные 20%, 480г", 14100, secondCategory, new URL(root + "images/photo/3052/3052415B.jpg?1355912251")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне топленое ультрапастеризованное 3,2%, 950г", 7910, secondCategory, new URL(root + "images/photo/3049/3049010B.jpg?1337875603")),
        ExtractedEntry("Utkonos", "Коктейль молочный Смешарики Ванильное мороженое 2,5%, 210г", 2660, secondCategory, new URL(root + "images/photo/3168/3168200B.jpg?1401444817")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 6%, 950г", 9280, secondCategory, new URL(root + "images/photo/3051/3051412B.jpg?1349859446")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино ультрапастеризованное 1,5%, 0,95л", 6650, secondCategory, new URL(root + "images/photo/3051/3051962B.jpg?1343154179")),
        ExtractedEntry("Utkonos", "Молоко Из Вологды ультрапастеризованное 3,2%, 0,2л", 2150, secondCategory, new URL(root + "images/photo/3149/3149705B.jpg?1397550555")),
        ExtractedEntry("Utkonos", "Сливки Домик в деревне 10%, 750г", 10900, secondCategory, new URL(root + "images/photo/3060/3060250B.jpg?1401443557")),
        ExtractedEntry("Utkonos", "Сливки Белый город питьевые ультрапастеризованные 10%, 200г", 3450, secondCategory, new URL(root + "images/photo/3109/3109097B.jpg?1411107810")),
        ExtractedEntry("Utkonos", "Сливки Простоквашино 10%, 350г", 6190, secondCategory, new URL(root + "images/photo/3053/3053759B.jpg?1365595611")),
        ExtractedEntry("Utkonos", "Сливки Петмол ультрапастеризованные 11%, 500г", 8970, secondCategory, new URL(root + "images/photo/3045/3045746B.jpg?1373873420")),
        ExtractedEntry("Utkonos", "Молоко Агуша Я сам Иммунити стерилизованное с витаминами и минералами 2,5%, 0,5л", 5240, secondCategory, new URL(root + "images/photo/3127/3127474B.jpg?1401443666")),
        ExtractedEntry("Utkonos", "Молоко Простоквашино топленое 4%, 0,93л", 6630, secondCategory, new URL(root + "images/photo/3054/3054784B.jpg?1342714213")),
        ExtractedEntry("Utkonos", "Сливки Петмол для взбивания ультрапастеризованные 33%, 0,5л", 16100, secondCategory, new URL(root + "images/photo/3052/3052276B.jpg?1373871791")),
        ExtractedEntry("Utkonos", "Молоко Домик в деревне ультрапастеризованное 2,5%, 1450г", 9840, secondCategory, new URL(root + "images/photo/3053/3053618B.jpg?1401443411")),
        ExtractedEntry("Utkonos", "Молоко Брест-Литовск ультрапастеризованное 2,8%, 1л", 6020, secondCategory, new URL(root + "images/photo/3189/3189859B.jpg?1397111402")),
        ExtractedEntry("Utkonos", "Молоко Parmalat ультрапастеризованное Диеталат 0,5%, 1л", 6530, secondCategory, new URL(root + "images/photo/3109/3109122B.jpg?1398335109")),
        ExtractedEntry("Utkonos", "Коктейль молочный Смешарики Карамельная ириска 2,5%, 210г", 2660, secondCategory, new URL(root + "images/photo/3168/3168244B.jpg?1401444807")),
        ExtractedEntry("Utkonos", "Молоко Белый город ультрапастеризованное 2,5%, 1л", 5760, secondCategory, new URL(root + "images/photo/3109/3109140B.jpg?1398335035")),
        ExtractedEntry("Utkonos", "Молоко Тема питьевое ультрапастеризованное для детей с 8 месяцев 3,2%, 200г", 2160, secondCategory, new URL(root + "images/photo/3045/3045824B.jpg?1365606319")),
        ExtractedEntry("Utkonos", "Молоко Брест-Литовск питьевое ультрапастеризованное 3,6%, 1л", 6570, secondCategory, new URL(root + "images/photo/3189/3189413B.jpg?1395317150")),
        ExtractedEntry("Utkonos", "Молоко Ясный луг ультрапастеризованное 1,5%, 1л", 5720, secondCategory, new URL(root + "images/photo/3184/3184964B.jpg?1365431481")),
        ExtractedEntry("Utkonos", "Молоко Белый город ультрапастеризованное 3,2%, 1л", 6210, secondCategory, new URL(root + "images/photo/3109/3109165B.jpg?1398335030")),
        ExtractedEntry("Utkonos", "Молоко Новая деревня пастеризованное 2,5%, 1л", 8090, secondCategory, new URL(root + "images/photo/3143/3143574B.jpg?1401534749")),
        ExtractedEntry("Utkonos", "Коктейль молочный Смешарики Клубника 2,5%, 210г", 2660, secondCategory, new URL(root + "images/photo/3172/3172383B.jpg?1401444875"))

      )
    )
  }
}

# SkillWars-Practice

- LeaderBoard GUI (Te aparezcan los top 10 de elo de todos los kits de ranked) (✓)
- Editor de Kits (✓)
- Tournament (✓)
- Comando /settings (GUI donde podes desactivar/activar la scoreboard y desactivar/activar peticiones de duelo, activar/desactivar que gente pueda espectearte (solo para vips), para poner de dia o de noche, activar/desactivar visibilidad del chat solo para vos) (✓)
- Scoreboard que aparezcan los rangos y que el tab este dividido por mundos. (✓)
- Scoreboard editable + variables: %evento% Con esta variable te aparecera si hay algun evento activo y si no hay ninguno activo que diga "&cNinguno" (✓)
- Con /eventos te abre una GUI donde seleccionas cual queres hostear. Si uno esta iniciado otra persona no puede iniciar otro. Y deberan esperar 30 segundos despues de que termine el evento abierto para hostear otro. (✓)
- Evento NoDebuff Lite: Bracket de NoDebuff (✓)
- Comando para modificar el elo a los usuarios (✓)
- Evento Teamfight: Entre todos los que entren al evento se dividen 2 equipos (✓)

*EVENTOS

- Evento FFA: En un mapa se hace todos contra todos (✓)
- Evento SUMO: Bracket de Sumo (✓)

*PARTY

- Cola 2v2 Ranked y Unranked (✓)
- Pelea contra otras partys (✓)
- Pelea contra tu misma party. Split: Se hacen 2 equipos entre todos tus miembros - FFA: Todos contra todos. - RedOver. (✓)
- Chat Publico y Chat de party (✓)
  
#COSAS QUE FALTAN

- Colas Ranked y Unranked (Que los kits puedas ubicarlos a tu gusto en el menu)
- Configuracion en GUI de party (Limite de miembros con un permiso para que los vips tengan mas slots, que la party sea publica + broadcast)

--------------------------------------
ARENAS

/arena create <nombre>
/arena delete <nombre>
/arena spawn1 <nombre>
/arena spawn2 <nombre>
/arena center <nombre> (Esto cuando se empiece algun evento o party de FFA se pondran todos los jugadores repartidos en el centro)
/arena kit <kit> (Con este comando seleccionas que kits queres en esta arena)
/arena list

KITS

/ladder create <kit>
/ladder delete <kit>
/ladder seticon <kit> (Para setear el icono del kit)
/ladder setinv <kit> (Te setea todo el inventario + efectos y todo)
/ladder load <kit> (Para que te de el inventario y todo del kit)
/ladder ranked <kit> (Por defecto todos los kits seran solo unranked, y con este comando habilitaras si queres que se juegen en ranked)
/ladder editable <kit> (Para activar los kits que se puedan editar)
  
  Comandos:
/leave - para salir de cualquier evento
/evento <evento> setspawn

/ffa center
/ffa join

/sumo setspawn1
/sumo setspawn2
/sumo join

/ndl setspawn1
/ndl setspawn2
/ndl join

/tf setspawn1
/tf setspawn2
/tf join

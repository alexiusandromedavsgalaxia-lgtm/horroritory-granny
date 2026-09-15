package com.horroritory.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        setContentView(new HorrorView(this));
    }
}

class HorrorView extends View {
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Random rng = new Random();
    final String[] modes = {"EASY","NORMAL","HARD","EXTREME","IMPOSSIBLE","CRAZY","SUPER CRAZY","AE [❔]"};
    final String[] altPlaces = {"un cine sin público","un vagón de tren vacío","un invernadero en un tejado","una lavandería abandonada","un supermercado que no aparece en ningún mapa","un pasillo de mantenimiento que no debería existir"};
    int selected = 1;
    boolean menu = true, dead = false, won = false, alternative = false;
    boolean moving = false, sprint = false;
    float px, py, joyX, joyY, camX, camY;
    int stage = 0;
    long last;
    float eventTimer = 0, fear = 0;
    final ArrayList<Wall> walls = new ArrayList<>();
    final ArrayList<Item> items = new ArrayList<>();
    final ArrayList<Enemy> enemies = new ArrayList<>();
    final ArrayList<Rat> rats = new ArrayList<>();
    RectF exit = new RectF();

    HorrorView(Context c) { super(c); p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); setFocusable(true); }
    float enemyMult() { return new float[]{.70f,1f,1.15f,1.30f,1.50f,2f,2.50f,3f}[selected]; }
    int enemyCount() { return selected == 7 ? 3 : selected >= 5 ? 2 : 1; }
    float visibility() { return new float[]{1f,1f,.90f,.75f,.65f,.45f,.25f,.10f}[selected]; }
    float creakChance() { return new float[]{.10f,.20f,.35f,.50f,.60f,.70f,.80f,.85f}[selected]; }

    void startGame() {
        menu=false; dead=false; won=false; alternative=false; stage=0; fear=0; eventTimer=0;
        px=180; py=170; camX=camY=0; items.clear(); enemies.clear(); rats.clear(); walls.clear();
        buildHouse();
        items.add(new Item(1040,150,"LLAVE",0));
        items.add(new Item(250,650,"FUSIBLE",1));
        items.add(new Item(1030,650,"PALANCA",2));
        items.add(new Item(600+rng.nextInt(300),350+rng.nextInt(220),"PIEZA",3));
        for(int i=0;i<enemyCount();i++) enemies.add(new Enemy(1160-i*130,270+i*180));
        if(selected>=2) for(int i=0;i<4+selected;i++) rats.add(new Rat(180+rng.nextInt(950),150+rng.nextInt(570)));
        exit.set(1280,690,1370,760);
        last=System.nanoTime(); invalidate();
    }

    void buildHouse() {
        float W=1500,H=850;
        walls.add(new Wall(60,60,W-60,90)); walls.add(new Wall(60,H-60,W-60,H-30));
        walls.add(new Wall(60,60,90,H-30)); walls.add(new Wall(W-90,60,W-60,H-30));
        walls.add(new Wall(60,230,440,260)); walls.add(new Wall(540,230,930,260)); walls.add(new Wall(1030,230,W-60,260));
        walls.add(new Wall(220,90,250,230)); walls.add(new Wall(720,90,750,230)); walls.add(new Wall(1180,90,1210,230));
        walls.add(new Wall(60,430,300,460)); walls.add(new Wall(400,430,700,460)); walls.add(new Wall(800,430,1100,460)); walls.add(new Wall(1200,430,W-60,460));
        walls.add(new Wall(340,460,370,H-60)); walls.add(new Wall(930,460,960,H-60));
        walls.add(new Wall(60,650,180,680)); walls.add(new Wall(340,650,560,680)); walls.add(new Wall(720,650,900,680)); walls.add(new Wall(1060,650,1230,680));
    }

    boolean blocked(float x,float y) {
        if(x<100||y<100||x>1400||y>780) return true;
        for(Wall w:walls) if(x>w.x1-16&&x<w.x2+16&&y>w.y1-16&&y<w.y2+16) return true;
        return false;
    }
    void movePlayer(float dx,float dy) {
        float nx=px+dx,ny=py+dy;
        if(!blocked(nx,py)) px=nx;
        if(!blocked(px,ny)) py=ny;
    }
    boolean lineClear(float ax,float ay,float bx,float by) {
        int n=(int)(Math.hypot(bx-ax,by-ay)/12)+1;
        for(int i=1;i<n;i++) { float t=i/(float)n,x=ax+(bx-ax)*t,y=ay+(by-ay)*t; if(blocked(x,y)) return false; }
        return true;
    }

    void update(float dt) {
        for(Enemy e:enemies) e.update(dt,this);
        for(Rat r:rats) r.update(dt,this);
        if(moving) {
            float speed=(sprint?285:190)*dt;
            movePlayer(joyX*speed,joyY*speed);
        }
        camX=Math.max(0,Math.min(1500-getWidth(),px-getWidth()/2f));
        camY=Math.max(0,Math.min(850-getHeight(),py-getHeight()/2f));
        eventTimer-=dt;
        if(eventTimer<=0) { eventTimer=2.5f+rng.nextFloat()*5f; if(rng.nextFloat()<creakChance()) fear=Math.min(1,fear+.18f); }
        fear=Math.max(0,fear-dt*.025f);
        if(stage==0 && has(0)) stage=1;
        if(stage==1 && has(1)) stage=2;
        if(stage==2 && has(2)) stage=3;
        if(stage==3 && exit.contains(px,py)) {
            won=true; alternative=rng.nextFloat()<.28f; menu=false; invalidate(); return;
        }
        for(Enemy e:enemies) if(Math.hypot(e.x-px,e.y-py)<34) { dead=true; menu=false; invalidate(); return; }
    }
    boolean has(int type) { for(Item i:items) if(i.type==type&&i.taken) return true; return false; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if(menu) drawMenu(c); else { update(.016f); drawWorld(c); if(dead||won) drawEnding(c); }
        postInvalidateDelayed(16);
    }

    void drawWorld(Canvas c) {
        c.drawColor(Color.rgb(7,7,9)); c.save(); c.translate(-camX,-camY);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(34,31,29)); c.drawRect(60,60,1440,790,p);
        p.setColor(Color.rgb(63,57,52)); for(Wall w:walls)c.drawRect(w.x1,w.y1,w.x2,w.y2,p);
        p.setColor(Color.rgb(38,75,50)); for(Item i:items) if(!i.taken){c.drawCircle(i.x,i.y,15,p); label(c,i.x-25,i.y-24,i.name);}
        p.setColor(stage>=3?Color.rgb(45,100,65):Color.rgb(70,70,75)); c.drawRect(exit,p); label(c,1265,680,"SALIDA");
        for(Rat r:rats){p.setColor(Color.rgb(85,85,85));c.drawCircle(r.x,r.y,8,p);}
        for(Enemy e:enemies) drawEnemy(c,e);
        p.setColor(Color.rgb(205,205,210)); c.drawCircle(px,py,15,p);
        p.setColor(Color.WHITE); c.drawCircle(px-5,py-3,2,p); c.drawCircle(px+5,py-3,2,p);
        if(selected==7) drawFog(c);
        c.restore();
        drawHud(c);
        drawTouch(c);
    }
    void drawEnemy(Canvas c,Enemy e){p.setColor(Color.rgb(155,155,160));c.drawCircle(e.x,e.y,22,p);p.setColor(Color.BLACK);c.drawCircle(e.x-7,e.y-4,3,p);c.drawCircle(e.x+7,e.y-4,3,p);}
    void drawFog(Canvas c) {
        p.setShader(new RadialGradient(px-camX,py-camY,220,new int[]{0x00000000,0xD0000000,0xF5000000},new float[]{0,.32f,1},Shader.TileMode.CLAMP));
        c.drawRect(0,0,getWidth(),getHeight(),p); p.setShader(null);
    }
    void drawHud(Canvas c){
        p.setColor(0xB5000000);c.drawRect(0,0,getWidth(),78,p);
        label(c,20,27,"HORRORITORY  ·  "+modes[selected]);
        String objective=stage==0?"encuentra la LLAVE":stage==1?"encuentra el FUSIBLE":stage==2?"encuentra la PALANCA":"llega a la SALIDA";
        label(c,20,52,"OBJETIVO: "+objective+"   ·   velocidad perseguidor x"+enemyMult());
        if(selected==7) label(c,20,getHeight()-18,"AE [❔]  ·  visión jugador 10%  ·  visión enemiga completa  ·  crujidos 85%+");
    }
    void drawTouch(Canvas c){
        p.setColor(0x44333333);c.drawCircle(95,getHeight()-105,68,p);p.setColor(0x88777777);c.drawCircle(95+joyX*35,getHeight()-105+joyY*35,25,p);
        p.setColor(0x55333333);c.drawCircle(getWidth()-90,getHeight()-95,48,p);label(c,getWidth()-104,getHeight()-89,"E");
        p.setColor(0x55333333);c.drawCircle(getWidth()-205,getHeight()-95,48,p);label(c,getWidth()-226,getHeight()-89,"RUN");
    }
    void drawMenu(Canvas c){
        c.drawColor(Color.rgb(5,5,7));p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(Color.WHITE);p.setTextSize(55);c.drawText("HORRORITORY",getWidth()/2f,125,p);
        p.setTypeface(Typeface.DEFAULT);p.setTextSize(18);p.setColor(Color.LTGRAY);c.drawText("escape de la casa · aventura de horror original",getWidth()/2f,160,p);
        p.setTextSize(26);c.drawText("MODO: "+modes[selected],getWidth()/2f,250,p);p.setTextSize(15);p.setColor(Color.GRAY);c.drawText("zona izquierda = anterior · zona derecha = siguiente",getWidth()/2f,280,p);
        p.setTextSize(28);p.setColor(Color.WHITE);c.drawText("EMPEZAR",getWidth()/2f,385,p);p.setTextSize(14);p.setColor(Color.GRAY);c.drawText("Android · paisaje · offline · guardado local",getWidth()/2f,420,p);
        p.setTextSize(13);c.drawText("AE: modo extremo · 3 perseguidoras · oscuridad casi total",getWidth()/2f,470,p);p.setTextAlign(Paint.Align.LEFT);
    }
    void drawEnding(Canvas c){
        p.setColor(0xE9000000);c.drawRect(0,0,getWidth(),getHeight(),p);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTextSize(38);
        c.drawText(won?"HAS ESCAPADO":"TE HAN ENCONTRADO",getWidth()/2f,210,p);p.setTextSize(18);
        if(won) { if(alternative) c.drawText("FINAL ALTERNATIVO · " + altPlaces[rng.nextInt(altPlaces.length)],getWidth()/2f,260,p); else c.drawText("FINAL NORMAL · la puerta queda atrás",getWidth()/2f,260,p); }
        else c.drawText("la casa vuelve a quedarse en silencio...",getWidth()/2f,260,p);
        c.drawText("toca para volver a jugar",getWidth()/2f,335,p);p.setTextAlign(Paint.Align.LEFT);
    }
    void label(Canvas c,float x,float y,String s){p.setTextSize(13);p.setColor(Color.WHITE);c.drawText(s,x,y,p);}

    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX(),y=e.getY();int a=e.getActionMasked();
        if(a==MotionEvent.ACTION_DOWN){
            if(menu){
                if(y>205&&y<315){if(x<getWidth()/2f)selected=(selected+7)%8;else selected=(selected+1)%8;invalidate();return true;}
                if(y>330&&y<455){startGame();return true;}
            } else if(dead||won){startGame();return true;}
            else { moving=true; updateJoy(x,y); if(x>getWidth()-270&&y>getHeight()-190){if(x>getWidth()-150) collect(); else sprint=true;} }
        } else if(a==MotionEvent.ACTION_MOVE&&!menu&&!dead&&!won){updateJoy(x,y);}
        else if(a==MotionEvent.ACTION_UP){moving=false;sprint=false;joyX=joyY=0;}
        return true;
    }
    void updateJoy(float x,float y){if(y>getHeight()-195&&x<245){float dx=x-95,dy=y-(getHeight()-105);joyX=Math.max(-1,Math.min(1,dx/68));joyY=Math.max(-1,Math.min(1,dy/68));}}
    void collect(){for(Item i:items)if(!i.taken&&Math.hypot(i.x-px,i.y-py)<70){i.taken=true;if(i.type==0)stage=Math.max(stage,1);if(i.type==1)stage=Math.max(stage,2);if(i.type==2)stage=Math.max(stage,3);}}

    static class Wall{float x1,y1,x2,y2;Wall(float a,float b,float c,float d){x1=a;y1=b;x2=c;y2=d;}}
    static class Item{float x,y;String name;int type;boolean taken;Item(float a,float b,String n,int t){x=a;y=b;name=n;type=t;}}
    static class Enemy{
        float x,y;int state=0;float targetX,targetY;long stateUntil;
        Enemy(float a,float b){x=a;y=b;targetX=a;targetY=b;}
        void update(float dt,HorrorView g){
            float d=(float)Math.hypot(g.px-x,g.py-y); boolean sees=d<620 && g.lineClear(x,y,g.px,g.py);
            if(g.selected==7) sees=d<900 && g.lineClear(x,y,g.px,g.py);
            if(sees){state=2;targetX=g.px;targetY=g.py;stateUntil=System.nanoTime()+1_500_000_000L;}
            else if(state==2 && System.nanoTime()>stateUntil){state=3;stateUntil=System.nanoTime()+2_500_000_000L;}
            if(state==0 || state==3){if(Math.hypot(targetX-x,targetY-y)<18){targetX=120+g.rng.nextInt(1250);targetY=120+g.rng.nextInt(620);}move(targetX,targetY,dt,45*g.enemyMult());}
            else if(state==2)move(g.px,g.py,dt,112*g.enemyMult());
        }
        void move(float tx,float ty,float dt,float sp){float dx=tx-x,dy=ty-y,d=(float)Math.hypot(dx,dy);if(d>1){x+=dx/d*sp*dt;y+=dy/d*sp*dt;}}
    }
    static class Rat{
        float x,y,tx,ty;Rat(float a,float b){x=a;y=b;tx=a;ty=b;}
        void update(float dt,HorrorView g){if(Math.hypot(tx-x,ty-y)<12){tx=Math.max(110,Math.min(1390,x+g.rng.nextInt(240)-120));ty=Math.max(110,Math.min(770,y+g.rng.nextInt(180)-90));}float dx=tx-x,dy=ty-y,d=(float)Math.hypot(dx,dy);if(d>1){x+=dx/d*38*dt;y+=dy/d*38*dt;}}
    }
}

package com.horroritory.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setFlags(1024,1024); setContentView(new HorrorView(this)); }
}

class HorrorView extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final Random rng=new Random();
    final String[] modes={"EASY","NORMAL","HARD","EXTREME","IMPOSSIBLE","CRAZY","SUPER CRAZY","AE [❔]"};
    int selected=1; boolean menu=true, dead=false, won=false; long last;
    float px,py; float joyX,joyY; boolean moving, sprint; int keys=0; float fear=0;
    ArrayList<Enemy> enemies=new ArrayList<>(); ArrayList<Rat> rats=new ArrayList<>(); ArrayList<Item> items=new ArrayList<>();
    RectF exit=new RectF();
    float[][] walls={{80,80,1100,110},{80,210,500,240},{650,210,1100,240},{80,340,300,370},{450,340,700,370},{850,340,1100,370},{80,470,550,500},{700,470,1100,500},{80,600,350,630},{500,600,800,630},{950,600,1100,630}};
    HorrorView(Context c){super(c); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); setFocusable(true);}
    float enemyMult(){int i=selected; return new float[]{.7f,1f,1.15f,1.3f,1.5f,2f,2.5f,3f}[i];}
    float visibility(){return new float[]{1f,1f,.9f,.75f,.65f,.45f,.25f,.1f}[selected];}
    int enemyCount(){return selected>=5 ? (selected==7 ? 3:2):1;}
    void startGame(){menu=false;dead=false;won=false;keys=0;fear=0;px=140;py=150;items.clear();enemies.clear();rats.clear();
        items.add(new Item(950,170,"LLAVE")); items.add(new Item(180,560,"FUSIBLE")); items.add(new Item(850,560,"PALANCA"));
        for(int i=0;i<enemyCount();i++) enemies.add(new Enemy(1000-i*90,280+i*100));
        if(selected>=2) for(int i=0;i<3+selected;i++) rats.add(new Rat(150+rng.nextInt(900),130+rng.nextInt(470)));
        exit.set(1020,690,1090,750); last=System.nanoTime(); invalidate(); }
    boolean blocked(float x,float y){ if(x<35||y<35||x>getWidth()-35||y>getHeight()-35)return true; for(float[]w:walls) if(x>w[0]-18&&x<w[2]+18&&y>w[1]-18&&y<w[3]+18)return true; return false; }
    void movePlayer(float dx,float dy){float nx=px+dx,ny=py+dy;if(!blocked(nx,py))px=nx;if(!blocked(px,ny))py=ny;}
    void update(float dt){ if(!moving)return; float speed=(sprint?260:175)*dt; movePlayer(joyX*speed,joyY*speed);
        for(Enemy e:enemies)e.update(dt,this); for(Rat r:rats)r.update(dt,this);
        if(selected==7 && rng.nextFloat()<dt*.8f) fear=Math.min(1,fear+.06f); else fear=Math.max(0,fear-dt*.03f);
        if(keys>=3 && exit.contains(px,py)){won=true;menu=false;invalidate();return;}
        for(Enemy e:enemies) if(Math.hypot(e.x-px,e.y-py)<34){dead=true;menu=false;invalidate();return;}
        last=System.nanoTime(); invalidate(); }
    @Override protected void onDraw(Canvas c){super.onDraw(c); if(menu){drawMenu(c);return;} drawWorld(c); if(dead||won)drawEnding(c); }
    void bg(Canvas c){c.drawColor(Color.rgb(8,8,10));p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(27,25,24));c.drawRect(45,45,getWidth()-45,getHeight()-45,p);}
    void drawWorld(Canvas c){bg(c);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(54,50,46));for(float[]w:walls)c.drawRect(w[0],w[1],w[2],w[3],p);
        p.setColor(Color.rgb(35,70,45));for(Item i:items)if(!i.taken){c.drawCircle(i.x,i.y,13,p);label(c,i.x-20,i.y-20,i.name);}
        p.setColor(Color.rgb(70,80,90));c.drawRect(exit,p);label(c,1010,680,"SALIDA");
        for(Rat r:rats){p.setColor(Color.rgb(80,80,80));c.drawCircle(r.x,r.y,7,p);}
        for(Enemy e){p.setColor(Color.rgb(145,145,145));c.drawCircle(e.x,e.y,19,p);p.setColor(Color.BLACK);c.drawCircle(e.x-6,e.y-3,3,p);c.drawCircle(e.x+6,e.y-3,3,p);}
        p.setColor(Color.rgb(200,200,205));c.drawCircle(px,py,14,p);
        if(keys<3){label(c,60,30,"MODO: "+modes[selected]);label(c,60,52,"OBJETOS: "+keys+"/3   ·   E: recoger / usar");}
        if(selected==7){p.setShader(new RadialGradient(px,py,160, new int[]{0x00000000,0xCC000000,0xF0000000},new float[]{0,.55f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);label(c,60,getHeight()-24,"AE: VISIÓN DEL JUGADOR 10% · PERSEGUIDORA: VISIÓN COMPLETA");}
        drawTouch(c);
    }
    void drawTouch(Canvas c){p.setColor(0x44333333);c.drawCircle(100,getHeight()-100,65,p);p.setColor(0x88777777);c.drawCircle(100+joyX*35,getHeight()-100+joyY*35,25,p);p.setColor(0x55333333);c.drawCircle(getWidth()-80,getHeight()-85,48,p);label(c,getWidth()-94,getHeight()-79,"E");}
    void drawMenu(Canvas c){c.drawColor(Color.rgb(5,5,7));p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(54);p.setColor(Color.WHITE);c.drawText("HORRORITORY",getWidth()/2f,150,p);p.setTextSize(18);p.setTypeface(Typeface.DEFAULT);c.drawText("escapa de la casa",getWidth()/2f,185,p);
        p.setTextSize(24);c.drawText("MODO: "+modes[selected],getWidth()/2f,270,p);p.setTextSize(16);c.drawText("◀   tocar aquí para cambiar   ▶",getWidth()/2f,305,p);p.setTextSize(26);p.setColor(Color.rgb(180,180,180));c.drawText("EMPEZAR",getWidth()/2f,410,p);p.setTextSize(13);p.setColor(Color.GRAY);c.drawText("táctil · paisaje · sin internet",getWidth()/2f,450,p);p.setTextAlign(Paint.Align.LEFT); }
    void drawEnding(Canvas c){p.setColor(0xDD000000);c.drawRect(0,0,getWidth(),getHeight(),p);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTextSize(36);String title=won?"HAS ESCAPADO":"TE HAN ENCONTRADO";c.drawText(title,getWidth()/2f,220,p);p.setTextSize(17);
        if(won){String[] places={"una lavandería abandonada","un cine sin público","un vagón de tren vacío","un invernadero en un tejado","un supermercado que no aparece en ningún mapa"};c.drawText("final alternativo: "+places[rng.nextInt(places.length)],getWidth()/2f,270,p);}else c.drawText("la casa vuelve a quedarse en silencio...",getWidth()/2f,270,p);
        c.drawText("toca para volver al menú",getWidth()/2f,350,p);p.setTextAlign(Paint.Align.LEFT); }
    void label(Canvas c,float x,float y,String s){p.setTextSize(13);p.setColor(Color.WHITE);c.drawText(s,x,y,p);}
    @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY(); if(e.getAction()==0){
            if(menu){if(y>230&&y<330){selected=(selected+1)%modes.length;invalidate();return true;}if(y>350&&y<480){startGame();return true;}if(x<getWidth()/2&&y>230&&y<330){selected=(selected+modes.length-1)%modes.length;invalidate();return true;}}
            else if(dead||won){startGame();return true;}else{moving=true;updateJoy(x,y); if(x>getWidth()-170&&y>getHeight()-180)collect();}
        }else if(e.getAction()==2&&!menu&&!dead&&!won){updateJoy(x,y);}else if(e.getAction()==1){moving=false;joyX=joyY=0;} return true;}
    void updateJoy(float x,float y){if(y>getHeight()-190&&x<240){float dx=x-100,dy=y-(getHeight()-100);float l=(float)Math.hypot(dx,dy);if(l>1){joyX=Math.max(-1,Math.min(1,dx/65));joyY=Math.max(-1,Math.min(1,dy/65));}}}
    void collect(){for(Item i:items)if(!i.taken&&Math.hypot(i.x-px,i.y-py)<65){i.taken=true;keys++;}}
    static class Item{float x,y;String name;boolean taken;Item(float x,float y,String n){this.x=x;this.y=y;name=n;}}
    static class Enemy{float x,y;Enemy(float x,float y){this.x=x;this.y=y;}void update(float dt,HorrorView g){float dx=g.px-x,dy=g.py-y,d=(float)Math.hypot(dx,dy);if(d<520){x+=dx/d*105*g.enemyMult()*dt;y+=dy/d*105*g.enemyMult()*dt;}else{x+=(float)Math.sin(System.nanoTime()/1e9+x)*12*dt;y+=(float)Math.cos(System.nanoTime()/1e9+y)*12*dt;}}}
    static class Rat{float x,y,tx,ty;Rat(float x,float y){this.x=x;this.y=y;tx=x;ty=y;}void update(float dt,HorrorView g){if(Math.hypot(tx-x,ty-y)<8){tx=x+g.rng.nextInt(160)-80;ty=y+g.rng.nextInt(120)-60;}float dx=tx-x,dy=ty-y,d=(float)Math.hypot(dx,dy);if(d>1){x+=dx/d*32*dt;y+=dy/d*32*dt;}}}
}

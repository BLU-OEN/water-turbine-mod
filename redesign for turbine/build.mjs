// Original, grid-authored Minecraft geometry and pixel materials. Node 18+, no dependencies.
import fs from 'node:fs';
import path from 'node:path';
import { deflateSync } from 'node:zlib';
import { fileURLToPath } from 'node:url';
const root = path.dirname(fileURLToPath(import.meta.url));
const turbine = path.basename(root).includes('turbine');
const kind = turbine ? 'turbine' : 'rebreather';
const ns = turbine ? 'waterturbine' : 'rebreathergoggles';
const pack = path.join(root, 'resource-pack');
const write = (p, v) => { fs.mkdirSync(path.dirname(p), {recursive:true}); fs.writeFileSync(p, v); };
const json = (p, v) => write(p, JSON.stringify(v, null, 2)+'\n');
const crcTable = Array.from({length:256}, (_,n)=>{for(let k=0;k<8;k++) n=n&1?0xedb88320^(n>>>1):n>>>1;return n>>>0;});
function crc(buf) {let c=0xffffffff;for(const b of buf)c=crcTable[(c^b)&255]^(c>>>8);return (c^0xffffffff)>>>0;}
function chunk(type, bytes) { const t=Buffer.from(type), b=Buffer.alloc(bytes.length+12);b.writeUInt32BE(bytes.length);t.copy(b,4);bytes.copy(b,8);b.writeUInt32BE(crc(Buffer.concat([t,bytes])),bytes.length+8);return b; }
function png(im) {const h=Buffer.alloc(13);h.writeUInt32BE(im.w);h.writeUInt32BE(im.h,4);h[8]=8;h[9]=6;const scan=Buffer.alloc((im.w*4+1)*im.h);for(let y=0;y<im.h;y++)Buffer.from(im.p.slice(y*im.w*4,(y+1)*im.w*4)).copy(scan,y*(im.w*4+1)+1);return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',h),chunk('IDAT',deflateSync(scan)),chunk('IEND',Buffer.alloc(0))]);}
function image(w,h,c='#00000000') {const im={w,h,p:new Uint8Array(w*h*4)};rect(im,0,0,w,h,c);return im;}
function color(c) {c=c.replace('#','');return [parseInt(c.slice(0,2),16),parseInt(c.slice(2,4),16),parseInt(c.slice(4,6),16),c.length===8?parseInt(c.slice(6,8),16):255];}
function pixel(im,x,y,c) {if(x<0||x>=im.w||y<0||y>=im.h)return;im.p.set(Array.isArray(c)?c:color(c),(Math.floor(y)*im.w+Math.floor(x))*4);}
function rect(im,x,y,w,h,c) {for(let j=y;j<y+h;j++)for(let i=x;i<x+w;i++)pixel(im,i,j,c);}
function copy(dst,src,x,y) {for(let j=0;j<src.h;j++)for(let i=0;i<src.w;i++)pixel(dst,x+i,y+j,[...src.p.slice((j*src.w+i)*4,(j*src.w+i)*4+4)]);}
const palette={copper:'#b8764e',light:'#e2ad78',shade:'#714b3d',patina:'#4c8275',mint:'#9bc1a6',dark:'#243b3d',black:'#14282d',steel:'#7c9291',white:'#dbddc4',lens:'#367e85',glint:'#b9e8d8'};
const atlas=image(64,64);
const tiles={};
function tile(name,index,base,decorate=()=>{}) {const im=image(16,16,base);for(let y=0;y<16;y++)for(let x=0;x<16;x++){const n=(x*13+y*31+x*y*7)%29;if(n<3){const c=color(base);pixel(im,x,y,c.map((v,i)=>i===3?v:Math.min(255,v+(n===0?8:-5))));}}decorate(im);const x=(index%4)*16,y=Math.floor(index/4)*16;copy(atlas,im,x,y);tiles[name]=[x/4,y/4,(x+16)/4,(y+16)/4];return im;}
function frame(im,a,b) {rect(im,0,0,16,1,a);rect(im,0,0,1,16,a);rect(im,0,15,16,1,b);rect(im,15,0,1,16,b);}
tile('copper',0,palette.copper,im=>{frame(im,palette.light,palette.shade);rect(im,3,4,4,1,'#c38b5f');rect(im,10,11,3,1,palette.shade);});
tile('patina',1,palette.patina,im=>{frame(im,'#78a28c','#315a54');rect(im,2,11,3,3,'#639888');rect(im,11,2,3,2,'#b18154');rect(im,12,12,2,2,'#a87850');});
tile('dark',2,palette.dark,im=>frame(im,'#496361',palette.black));
tile('steel',3,palette.steel,im=>{frame(im,'#b1bca9','#465e60');rect(im,3,3,8,1,'#a8b6a7');});
tile('lens',4,palette.lens,im=>{for(let y=0;y<16;y++)for(let x=0;x<16;x++)pixel(im,x,y,y>10?'#28565f':y>5?'#3b8d91':'#62b4ae');rect(im,2,2,3,1,palette.glint);rect(im,2,3,2,4,'#87d3bf');rect(im,5,7,2,2,'#87d3bf');rect(im,10,12,4,1,'#173f4c');frame(im,'#80bbad','#14373f');});
tile('grille',5,palette.black,im=>{for(let y=2;y<15;y+=3){rect(im,2,y,12,1,'#8a9d93');rect(im,2,y+1,12,1,'#3c5659');}frame(im,'#b17f56','#142629');});
tile('panel',6,palette.patina,im=>{frame(im,'#9daa88','#30514c');for(let y=4;y<13;y+=3){rect(im,4,y,8,1,'#263e3f');rect(im,4,y+1,8,1,'#7ca492');}for(const x of [1,13])for(const y of [1,13])rect(im,x,y,2,2,'#cfad78');});
tile('port',7,palette.dark,im=>{rect(im,3,3,10,10,'#b9794d');rect(im,4,4,8,8,'#684735');rect(im,5,5,6,6,'#172d32');rect(im,7,5,2,6,'#d6ad70');rect(im,5,7,6,2,'#d6ad70');frame(im,'#52695e','#14282d');});
tile('label',8,'#d9d9ba',im=>{frame(im,'#eee9cd','#8c997f');rect(im,3,3,2,9,'#324e4c');rect(im,7,3,6,2,'#324e4c');rect(im,7,7,4,1,'#324e4c');rect(im,7,10,6,2,'#324e4c');});
tile('strap',9,'#283738',im=>{rect(im,0,2,16,1,'#647268');rect(im,0,13,16,1,'#101f25');for(let x=2;x<16;x+=4)rect(im,x,6,1,3,'#4d6158');});
tile('bolt',10,'#263b3e',im=>{rect(im,3,3,10,10,'#aeb7a0');rect(im,4,4,8,8,'#718986');rect(im,7,5,2,6,'#294144');rect(im,5,7,6,2,'#294144');});
tile('lamp',11,'#28575a',im=>{frame(im,'#3c776e','#173c43');rect(im,3,3,10,10,'#71c8a9');rect(im,5,4,6,3,'#c1e3b6');});
tile('skin',12,'#aa9d82',im=>{rect(im,0,0,16,4,'#4b5146');rect(im,1,4,3,2,'#4b5146');rect(im,2,8,3,1,'#514f43');rect(im,11,8,3,1,'#514f43');rect(im,7,10,2,2,'#85775f');rect(im,5,13,6,1,'#85775f');});
tile('hair',13,'#4b5146',im=>{rect(im,2,3,3,8,'#3c443a');rect(im,10,7,2,7,'#5c6050');});
tile('cloth',14,'#688176',im=>{frame(im,'#8b9a87','#405950');});
tile('rib',15,'#9a6245',im=>{for(let x=2;x<16;x+=4){rect(im,x,0,1,16,'#e2aa72');rect(im,x+1,0,1,16,'#674739');}});
const assets=path.join(pack,'assets',ns);
write(path.join(assets,'textures/redesign/materials.png'),png(atlas));
const elements=[];
const faceNames=['north','south','east','west','up','down'];
function cube(name,from,to,mat,group='body',faceOverrides={}) {const uv=tiles[mat];const faces=Object.fromEntries(faceNames.map(f=>[f,{uv:[...uv],texture:'#materials'}]));Object.assign(faces,faceOverrides);const e={name,from,to,faces};elements.push({...e,group});return e;}
function face(mat) {return {uv:[...tiles[mat]],texture:'#materials'};}
function rotor(step) {const im=image(32,32,'#162c31');for(let y=0;y<32;y++)for(let x=0;x<32;x++){let dx=x-15.5,dy=y-15.5,r=Math.hypot(dx,dy);if(r>14.5)pixel(im,x,y,'#304949');else if(r>12.9)pixel(im,x,y,'#758f80');else if(r>11.7)pixel(im,x,y,'#091e25');else {const a=((Math.atan2(dy,dx)-step*Math.PI/32+r*.042)%(Math.PI/2)+Math.PI/2)%(Math.PI/2);if(r>3&&a<.53)pixel(im,x,y,a<.17?'#d4be8f':'#7f9d94');if(r<3)pixel(im,x,y,r<1.5?'#e8be7e':'#9c6b47');}}return im;}
if(turbine) {
  // Full one-block envelope, mechanically inset front and rear; collision remains a full cube.
  cube('Pressure vessel',[1,1,3],[15,15,13],'dark','body',{east:face('panel'),west:face('panel'),up:face('patina')});
  cube('Lower skid',[0,0,0],[16,2,16],'patina','body');
  cube('Upper casing',[0,14,0],[16,16,16],'patina','body',{up:face('panel')});
  for(const x of [0,14])for(const z of [0,14])cube('Corner rail',[x,2,z],[x+2,14,z+2],'copper','body');
  for(const z of [0,14]) {
    const group=z===0?'front':'rear';
    cube('Intake frame left',[2,2,z],[4,14,z+2],'steel',group);
    cube('Intake frame right',[12,2,z],[14,14,z+2],'steel',group);
    cube('Intake frame foot',[4,2,z],[12,4,z+2],'steel',group);
    cube('Intake frame crown',[4,12,z],[12,14,z+2],'steel',group);
    for(const x of [3,11])for(const y of [3,11])cube('Stepped ring',[x,y,z],[x+2,y+2,z+2],'steel',group);
    const plane=z===0?[4,4,2.05]:[4,4,13.55];
    const e=cube('Four vane rotor',plane,[12,12,plane[2]+.4],'dark',group);
    e.faces[z===0?'north':'south']={uv:[0,0,16,16],texture:'#rotor'};
    // The shallow hub gives the animated face a real center bearing.
    cube('Copper spindle',[7,7,z===0?1.4:14],[9,9,z===0?2.1:14.6],'copper',group);
    for(const x of [.3,14.3])for(const y of [2.7,12.3])cube('Fastener',[x,y,z===0?-.02:15.6],[x+1.4,y+1.4,z===0?.25:16.02],'bolt',group);
  }
  for(const x of [.65,15.05])cube('FE coupling',[x,6,6],[x+.3,10,10],'port','body');
  cube('Top FE coupling',[6,16,6],[10,16.1,10],'port','body');
  cube('Bottom FE coupling',[6,-.1,6],[10,0,10],'port','body');
  cube('Maker plate',[5.2,14.35,-.12],[9.8,15.65,.05],'label','front');
  cube('Water state lamp',[11,14.5,-.15],[12.5,15.5,.05],'lamp','front');
  const strip=image(32,32*16);for(let n=0;n<16;n++)copy(strip,rotor(n),0,n*32);
  write(path.join(assets,'textures/redesign/rotor_wet.png'),png(strip));
  write(path.join(assets,'textures/redesign/rotor_dry.png'),png(rotor(0)));
  json(path.join(assets,'textures/redesign/rotor_wet.png.mcmeta'),{animation:{interpolate:false,frametime:2,width:32,height:32}});
} else {
  // This 3D model is used in hand, GUI, ground and item frames. Armor uses its own vanilla UV map.
  cube('Left rubber gasket',[.5,7.3,4.1],[7.3,14.3,6.1],'dark','lenses');
  cube('Right rubber gasket',[8.7,7.3,4.1],[15.5,14.3,6.1],'dark','lenses');
  for(const x of [1,9]) {
    cube('Copper lens frame',[x,8,3.3],[x+6,14,4.7],'copper','lenses');
    cube('Sea glass lens',[x+.7,8.7,3.15],[x+5.3,13.3,3.35],'lens','lenses');
    cube('Upper brow',[x-.2,13.4,3],[x+6.2,14.4,4.9],'patina','lenses');
    cube('Outer hinge',[x===1?0:15,9.4,4],[x===1?1:16,12.2,6],'bolt','lenses');
  }
  cube('Bridge',[6.7,10.4,4],[9.3,12.1,5.7],'copper','lenses');
  cube('Nose seal',[6.5,7,4.4],[9.5,10.7,6.2],'dark','regulator');
  cube('Breathing manifold',[4.4,3.5,4.4],[11.6,7.9,6.4],'dark','regulator');
  cube('Regulator bezel',[5.5,3.3,3.5],[10.5,7.5,4.6],'copper','regulator');
  cube('Regulator slats',[6,3.8,3.3],[10,7,3.6],'grille','regulator');
  for(const x of [2.5,11]) {
    cube('Filter body',[x,4.2,4],[x+2.5,7.2,6.8],'patina','regulator');
    cube('Filter cap',[x-.2,4,3.6],[x+2.7,7.4,4.3],'copper','regulator',{north:face('grille')});
  }
  for(const x of [.4,14.5])cube('Woven side strap',[x,9.2,6],[x+1.1,11.2,12.8],'strap','strap');
  cube('Rear strap',[1.5,9.2,12],[14.5,11.2,13],'strap','strap');
  cube('Rear adjustment buckle',[6.3,8.8,12.8],[9.7,11.6,13.4],'steel','strap',{south:face('bolt')});
  const armor=image(128,64);
  // Vanilla head box UVs: top (16,0), bottom (32,0), right (0,16), front (16,16), left (32,16), back (48,16).
  const f=image(16,16);
  rect(f,0,6,16,3,'#223939');
  for(const x of [0,9]){rect(f,x,4,7,7,palette.dark);rect(f,x,4,7,1,palette.patina);rect(f,x+1,5,5,5,palette.copper);rect(f,x+2,6,3,3,palette.lens);rect(f,x+2,6,2,1,palette.glint);rect(f,x+2,7,1,1,'#7fbaa8');rect(f,x+1,9,5,1,palette.shade);}
  rect(f,7,6,2,2,palette.copper);rect(f,6,10,4,4,palette.dark);
  rect(f,5,11,6,4,palette.copper);rect(f,6,12,4,2,palette.black);rect(f,6,12,4,1,palette.steel);
  for(const x of [3,11]){rect(f,x,11,2,3,palette.patina);pixel(f,x,11,palette.mint);}
  copy(armor,f,16,16);
  for(const ox of [0,32,48]){rect(armor,ox,22,16,3,'#243a39');rect(armor,ox,22,16,1,'#698478');for(let x=2;x<16;x+=4)pixel(armor,ox+x,24,'#9ca88e');}
  // Buckle at back and copper hinges directly adjoining the front face.
  rect(armor,54,21,4,5,palette.copper);rect(armor,55,22,2,3,palette.dark);
  rect(armor,13,21,3,5,palette.copper);rect(armor,32,21,3,5,palette.copper);
  rect(armor,14,22,1,3,palette.steel);rect(armor,33,22,1,3,palette.steel);
  write(path.join(assets,'textures/models/armor/rebreather_layer_1.png'),png(armor));
  // An explicit inventory sprite is included as an editable alternate to the default 3D item.
  const icon=image(32,32);for(let y=0;y<16;y++)for(let x=0;x<16;x++)rect(icon,x*2,y*2,2,2,[...f.p.slice((y*16+x)*4,(y*16+x)*4+4)]);
  write(path.join(assets,'textures/item/rebreather_helmet.png'),png(icon));
}
const model={credit:'BLU-OEN / Aquifer redesign',parent:turbine?'minecraft:block/block':undefined,ambientocclusion:true,gui_light:'side',textures:{particle:`${ns}:redesign/materials`,materials:`${ns}:redesign/materials`,...(turbine?{rotor:`${ns}:redesign/rotor_dry`}:{})},elements:elements.map(({group,...e})=>e),display:{gui:{rotation:[25,225,0],translation:turbine?[0,0,0]:[0,0,0],scale:turbine?[.63,.63,.63]:[.85,.85,.85]},ground:{rotation:[0,0,0],translation:[0,3,0],scale:[.4,.4,.4]},fixed:{rotation:[0,180,0],translation:[0,0,0],scale:[.65,.65,.65]},firstperson_righthand:{rotation:[0,45,0],translation:[0,1,0],scale:[.5,.5,.5]},firstperson_lefthand:{rotation:[0,225,0],translation:[0,1,0],scale:[.5,.5,.5]},thirdperson_righthand:{rotation:[75,45,0],translation:[0,2.5,0],scale:[.375,.375,.375]},thirdperson_lefthand:{rotation:[75,225,0],translation:[0,2.5,0],scale:[.375,.375,.375]}}};
if(turbine) {
  json(path.join(assets,'models/block/water_turbine.json'),model);
  json(path.join(assets,'models/block/water_turbine_wet.json'),{parent:'waterturbine:block/water_turbine',textures:{rotor:'waterturbine:redesign/rotor_wet'}});
  json(path.join(assets,'models/item/water_turbine.json'),{parent:'waterturbine:block/water_turbine'});
  const variants={};for(const [dir,y] of [['north',0],['east',90],['south',180],['west',270]])for(const wet of [false,true])variants[`facing=${dir},waterlogged=${wet}`]={model:`waterturbine:block/water_turbine${wet?'_wet':''}`,y};
  json(path.join(assets,'blockstates/water_turbine.json'),{variants});
} else json(path.join(assets,'models/item/rebreather_helmet.json'),model);
json(path.join(pack,'pack.mcmeta'),{pack:{pack_format:34,description:`Aquifer / ${turbine?'Water Turbine':'Rebreather Goggles'} redesign — Minecraft 1.21.1`}});
const packIcon=image(64,64,palette.dark);for(let y=8;y<56;y++)for(let x=8;x<56;x++){if(x<12||x>51||y<12||y>51)pixel(packIcon,x,y,palette.copper);}rect(packIcon,19,22,7,24,palette.mint);rect(packIcon,38,22,7,24,palette.mint);rect(packIcon,26,15,12,7,palette.mint);rect(packIcon,26,30,12,6,palette.mint);write(path.join(pack,'pack.png'),png(packIcon));
const walk=(dir)=>fs.readdirSync(dir,{withFileTypes:true}).sort((a,b)=>a.name.localeCompare(b.name)).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]);
function zip(files) {let offset=0;const parts=[],central=[];for(const p of files){const b=fs.readFileSync(p),name=Buffer.from(path.relative(pack,p).split(path.sep).join('/')),c=crc(b),h=Buffer.alloc(30);h.writeUInt32LE(0x04034b50);h.writeUInt16LE(20,4);h.writeUInt16LE(0x800,6);h.writeUInt16LE(0x21,12);h.writeUInt32LE(c,14);h.writeUInt32LE(b.length,18);h.writeUInt32LE(b.length,22);h.writeUInt16LE(name.length,26);parts.push(h,name,b);const d=Buffer.alloc(46);d.writeUInt32LE(0x02014b50);d.writeUInt16LE(20,4);d.writeUInt16LE(20,6);d.writeUInt16LE(0x800,8);d.writeUInt16LE(0x21,14);d.writeUInt32LE(c,16);d.writeUInt32LE(b.length,20);d.writeUInt32LE(b.length,24);d.writeUInt16LE(name.length,28);d.writeUInt32LE(offset,42);central.push(d,name);offset+=h.length+name.length+b.length;}const cb=Buffer.concat(central),end=Buffer.alloc(22);end.writeUInt32LE(0x06054b50);end.writeUInt16LE(files.length,8);end.writeUInt16LE(files.length,10);end.writeUInt32LE(cb.length,12);end.writeUInt32LE(offset,16);return Buffer.concat([...parts,cb,end]);}
const zipData=zip(walk(pack));write(path.join(root,`redesign-for-${kind}.zip`),zipData);
const textures={materials:{url:'data:image/png;base64,'+png(atlas).toString('base64')}};
if(turbine)textures.rotor={url:'data:image/png;base64,'+fs.readFileSync(path.join(assets,'textures/redesign/rotor_wet.png')).toString('base64')};
else {const armorPath=path.join(assets,'textures/models/armor/rebreather_layer_1.png');textures.armor={url:'data:image/png;base64,'+fs.readFileSync(armorPath).toString('base64')};}
const data={kind,ns,elements,tiles,textures,download:'data:application/zip;base64,'+zipData.toString('base64'),model};
const html=fs.readFileSync(path.join(root,'preview-template.html'),'utf8').replace('/*__DESIGN_DATA__*/',JSON.stringify(data));
write(path.join(root,`redesign for ${kind}.html`),html);
json(path.join(root,`redesign for ${kind}.json`),model);
console.log(`Built ${kind}: ${elements.length} cuboids, ${walk(pack).length} pack files, ${zipData.length} ZIP bytes.`);

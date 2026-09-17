// Validate the shipped Minecraft assets, including the separate equipped armor layout.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import {inflateSync} from 'node:zlib';
import {fileURLToPath} from 'node:url';
const root=path.dirname(fileURLToPath(import.meta.url)), turbine=path.basename(root).includes('turbine');
const kind=turbine?'turbine':'rebreather',ns=turbine?'waterturbine':'rebreathergoggles',pack=path.join(root,'resource-pack');
const walk=d=>fs.readdirSync(d,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(d,e.name)):[path.join(d,e.name)]);
const read=p=>JSON.parse(fs.readFileSync(p,'utf8'));
assert.equal(read(path.join(pack,'pack.mcmeta')).pack.pack_format,34);
const modelDir=path.join(pack,'assets',ns,'models');
function resolveModel(id){const [n,p]=id.split(':');return read(path.join(pack,'assets',n,'models',p+'.json'));}
function pngInfo(p){const b=fs.readFileSync(p);assert.equal(b.subarray(1,4).toString(),'PNG');assert.equal(b[24],8);assert.equal(b[25],6);const w=b.readUInt32BE(16),h=b.readUInt32BE(20),idat=[];for(let at=8;at<b.length;){const len=b.readUInt32BE(at),type=b.subarray(at+4,at+8).toString();if(type==='IDAT')idat.push(b.subarray(at+8,at+8+len));at+=12+len;}const raw=inflateSync(Buffer.concat(idat));assert.equal(raw.length,h*(w*4+1));for(let y=0;y<h;y++)assert.equal(raw[y*(w*4+1)],0);return{w,h,raw};}
for(const p of walk(modelDir)){const model=read(p),base=model.parent&&!model.parent.startsWith('minecraft:')?resolveModel(model.parent):{},textures={...base.textures,...model.textures};for(const [key,id] of Object.entries(textures)){const [n,t]=id.split(':');assert.ok(fs.existsSync(path.join(pack,'assets',n,'textures',t+'.png')),`${key}: missing ${id}`);}for(const e of model.elements??[]){for(let i=0;i<3;i++){assert.ok(e.from[i]<e.to[i],`Degenerate cuboid ${e.name}`);assert.ok(e.from[i]>=-16&&e.to[i]<=32);}for(const f of Object.values(e.faces)){assert.ok(textures[f.texture.slice(1)],`Missing texture variable ${f.texture}`);assert.equal(f.uv.length,4);assert.ok(f.uv.every(v=>v>=0&&v<=16));}}}
for(const p of walk(pack).filter(p=>p.endsWith('.png')))pngInfo(p);
if(turbine){const variants=read(path.join(pack,'assets',ns,'blockstates/water_turbine.json')).variants;assert.equal(Object.keys(variants).length,8);for(const [state,v]of Object.entries(variants)){resolveModel(v.model);assert.equal(v.model.endsWith('_wet'),state.endsWith('true'));}const a=path.join(pack,'assets',ns,'textures/redesign/rotor_wet.png'),info=pngInfo(a),meta=read(a+'.mcmeta').animation;assert.equal(info.w,meta.width);assert.equal(info.h/meta.height,16);assert.equal(meta.frametime,2);assert.ok(!fs.existsSync(a.replace('_wet','_dry')+'.mcmeta'));}
else {const a=pngInfo(path.join(pack,'assets',ns,'textures/models/armor/rebreather_layer_1.png'));assert.equal(a.w,128);assert.equal(a.h,64);let visible=0;for(let y=0;y<a.h;y++)for(let x=0;x<a.w;x++){const alpha=a.raw[y*(a.w*4+1)+1+x*4+3];if(alpha){visible++;assert.ok(x<64&&y>=16&&y<32,'Armor paint leaked outside head side UVs');}}assert.ok(visible>100,'Mask is empty');}
const html=fs.readFileSync(path.join(root,`redesign for ${kind}.html`),'utf8');assert.ok(!html.includes('/*__DESIGN_DATA__*/'));assert.ok(html.includes('data:application/zip;base64,'));assert.ok(!/<script[^>]+src=/.test(html));
console.log(`PASS ${kind}: model references, UVs, textures, ${turbine?'eight facing/water states and animation':'armor head UVs and transparency'}, offline preview.`);

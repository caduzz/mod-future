# Sistema de Biomas Dual - Magenta & Azure Glade

## Overview
Implementação de um segundo bioma "Azure Glade" (Azul Bebê) para coexistir com o bioma Magenta Grove na dimensão Creative Realm. O sistema usa multi-noise biome source para transição suave entre os biomas.

## 📦 Componentes Criados

### 1. Novos Blocos (7 blocos)
- **AZURE_MOSS_BLOCK** - Musgo azul base (cor ciano, som moss)
- **AZURE_MOSS_CARPET** - Tapete de musgo azul
- **AZURE_GRASS_BLOCK** - Bloco de grama azul
- **AZURE_GLOW_FLOWER** - Flor azul com brilho suave (light level 8)
- **AZURE_CAVE_VINES** - Vinhas azuis do teto
- **AZURE_CAVE_VINES_LIT** - Vinhas azuis iluminadas (light level 10)
- **AZURE_SOIL_BLOCK** - Solo azul para camadas profundas

**Localização:** `src/main/java/net/caduzz/futuremod/block/ModBlocks.java`

### 2. Biomas (2 biomas)
- **creative_realm_magenta_grove.json** - Bioma rosa/magenta refinado
  - Cores quentes: fog_color 16711935, sky 16744703
  - Sons: ambient.basalt_deltas com cherry grove music
  - Partículas: cherry leaves
  
- **creative_realm_azure_glade.json** - Novo bioma azul bebê
  - Cores frias: fog_color 14283882, sky 16774155
  - Sons: cave ambience com lush_caves music
  - Partículas: end_rod (brilho etéreo)
  - Temperatura mais baixa: 0.3

**Localização:** `src/main/resources/data/futuremod/worldgen/biome/`

### 3. Configuração de Mundo Dual
**Arquivo:** `src/main/resources/data/futuremod/dimension/creative_realm.json`

Utiliza **multi_noise biome source** para distribuição:
- **Weirdness -1.0 a 0.0** = Magenta Grove
- **Weirdness 0.0 a 1.0** = Azure Glade

Permite transição suave e natural entre os biomas.

### 4. Vegetação (3 features por bioma)

#### Bioma Magenta Grove
- **magenta_cherry_trees** - Árvores cherry clássicas
  - Tronco: cherry_wood
  - Folhas: cherry_leaves
  - Geração: 8 árvores por chunk
  
#### Bioma Azure Glade
- **azure_frostpine_trees** - Árvores spruce para ambiente frio
  - Tronco: spruce_wood
  - Folhas: spruce_leaves
  - Geração: 6 árvores por chunk
  
- **azure_moss_patch** - Patch de musgo azul
  - Composição: 80% tapete, 3% flor, 4% azalea, 7% azalea florida
  - Geração: 150 patches por chunk
  
- **azure_moss_vegetation** - Vegetação densa
  - Mais denso que azure_moss_patch
  - Geração: 180 blocos por chunk

**Localizações:**
- `src/main/resources/data/futuremod/worldgen/configured_feature/`
- `src/main/resources/data/futuremod/worldgen/placed_feature/`

### 5. Assets (Blockstates e Models)

#### Blockstates (8 arquivos)
- `azure_moss_block.json`
- `azure_moss_carpet.json`
- `azure_grass_block.json`
- `azure_glow_flower.json`
- `azure_cave_vines.json` (com variante berries)
- `azure_cave_vines_lit.json` (com variante berries)
- `azure_soil_block.json`

#### Block Models (9 arquivos)
- Reutilizam texturas vanilla com tintindex
- `azure_moss_block.json` - usa grass_block_top tinted
- `azure_cave_vines*.json` - herda cave_vines vanilla
- `azure_glow_flower.json` - usa blue_orchid da vanilla

#### Item Models (7 arquivos)
- `azure_*.json` - modelos de item para cada bloco

**Localização:**
- `src/main/resources/assets/futuremod/blockstates/`
- `src/main/resources/assets/futuremod/models/block/`
- `src/main/resources/assets/futuremod/models/item/`

## 🎨 Design Visual

### Cores Utilizadas
| Elemento | Magenta Grove | Azure Glade |
|----------|---------------|------------|
| Fog Color | FF FF FF (Magenta) | D9 CE BA (Azul claro) |
| Sky Color | FF C9 FF (Rosa claro) | FF E8 9B (Azul claro) |
| Water Color | FF FF FF (Magenta) | D9 CE BA (Azul) |
| Foliage | 9B 59 66 (Rosa) | 78 00 FF (Azul) |
| Grass | 9B 59 66 (Rosa) | 80 00 FF (Azul) |

### Atmosfera
- **Magenta:** Quente, vibrante, energético
- **Azure:** Frio, calmo, etéreo e mágico

## ✨ Recursos Especiais

### Brilho Luminoso
- `azure_glow_flower`: light level 8 (brilho suave)
- `azure_cave_vines_lit`: light level 10 (iluminação etérea)
- Cria ambience mágico no bioma

### Partículas
- Magenta: cherry_leaves (4/1000 chance)
- Azure: end_rod (3/1000 chance) - brilho etéreo

### Música
- Magenta: minecraft:music.overworld.cherry_grove
- Azure: minecraft:music.overworld.lush_caves

## 🔧 Integração Técnica

### BlockBehaviour Properties
Todos usando tintindex (color multiplier) no blockstate json:
```json
"tintindex": 0  // Aplica cor dinamicamente do bioma
```

### Biome Parameters (multi_noise)
O sistema usa parâmetros de ruído para gerar distribuição:
```
continentalness: [-1.0, 0.0] (ambos)
weirdness: [-1.0, 0.0] para Magenta
weirdness: [0.0, 1.0] para Azure
```

## 📊 Estatísticas de Geração

| Parâmetro | Magenta Grove | Azure Glade |
|-----------|--------------|-----------|
| Árvores/chunk | 8 | 6 |
| Moss patches/chunk | 125 | 150 |
| Moss vegetation/chunk | 125 | 180 |
| Ceiling vines/chunk | - | 80 |
| Downfall | 0.7 | 0.6 |
| Temperature | 0.5 | 0.3 |
| Precipitation | true | true |

## 🚀 Próximos Passos (Opcionais)

### Melhorias Possíveis
1. **Texturas Customizadas** - Criar texturas únicas em vez de reutilizar vanilla
2. **Estruturas Especiais** - Adicionar estruturas/árvores gigantes
3. **Creatures** - Mobs customizados para cada bioma
4. **Transição Suave** - River biome para transição entre magenta e azure
5. **Efeitos de Tempo** - Diferentes efeitos de chuva/neve
6. **Sons Customizados** - Ambient sounds exclusivos

## 📝 Notas de Desenvolvimento

- Todas as configurações usam JSON para facilitar tweaking
- Nenhuma classe Java customizada necessária (apenas blockstates vanilla)
- Sistema modular - fácil adicionar novos biomas futuramente
- Compilação: ✅ BUILD SUCCESSFUL

## 🎯 Resultado Final

Um sistema de biomas dual harmonioso onde:
- **Magenta Grove**: Região vibrante com cherry trees e musgo roxo
- **Azure Glade**: Região tranquila com spruce trees e musgo azul
- Transição suave via multi-noise biome source
- Ambos coexistem naturalmente na mesma dimensão


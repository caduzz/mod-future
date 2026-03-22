# Animated Java - Cigarro com animação de fumar

## Como exportar do Blockbench para substituir o modelo

1. **Crie o cigarro animado no Blockbench** com o plugin Animated Java instalado.

2. **Configuração do Blueprint (Blueprint Settings):**
   - **Export Namespace:** `futuremod`
   - **Display Item:** `futuremod:cigarette`
   - **Resource Pack Export Mode:** Raw (para pasta)
   - **Resource Pack path:** exporte para uma pasta temporária

3. **Arquivos a substituir:**
   - O Animated Java vai gerar modelos em `assets/futuremod/models/`
   - **Substitua** o arquivo:
     - `src/main/resources/assets/futuremod/models/item/cigarette.json`
   - **Copie** qualquer textura gerada para:
     - `src/main/resources/assets/futuremod/textures/item/`
   - **Copie** modelos adicionais (rig, bones, etc.) para:
     - `src/main/resources/assets/futuremod/models/`
   - **Copie** arquivos do data pack (se houver) para:
     - `src/main/resources/data/futuremod/`

4. **Estrutura final esperada:**
   ```
   src/main/resources/assets/futuremod/
   ├── models/
   │   ├── item/
   │   │   └── cigarette.json          ← substituir com seu export
   │   └── (outros modelos do rig, se houver)
   ├── textures/
   │   └── item/
   │       └── cigarette.png           ← textura do cigarro
   └── ANIMATED_JAVA_README.md
   ```

5. Depois de copiar os arquivos, faça rebuild do mod (`./gradlew build`).

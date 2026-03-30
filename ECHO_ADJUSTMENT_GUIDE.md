# Echo Adjustment Guide - Otimização Rápida
**Para ajustes ágeis sem recompilar manualmente**

---

## 🎯 Diagnóstico Rápido

Entra na Creative Realm e se faz uma dessas perguntas:

### ❓ "Sound do footstep fica abafado?"
```
SOLUÇÃO: Aumentar DRY_LEVEL
Arquivo: CreativeRealmEchoEffect.java:58
Antes:   private static final float REVERB_DRY_LEVEL = 0.98f;
Depois:  private static final float REVERB_DRY_LEVEL = 0.99f;

OU reduzir reverb:
Antes:   EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.55f);
Depois:  EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.45f);
```

### ❓ "Echo parece muito forte?"
```
SOLUÇÃO 1: Reduzir volume base
Arquivo: CreativeRealmEchoEffect.java:56
Antes:   private static final float[] ECHO_VOLUME_BASE = { 0.65f, 0.40f };
Depois:  private static final float[] ECHO_VOLUME_BASE = { 0.55f, 0.30f };

SOLUÇÃO 2: Aumentar decay (desaparece mais rápido)
Antes:   private static final float ECHO_DECAY_FACTOR = 0.60f;
Depois:  private static final float ECHO_DECAY_FACTOR = 0.50f;
```

### ❓ "Som muito seco, precisa de mais ambiente?"
```
SOLUÇÃO: Aumentar reverb
Antes:   private static final float REVERB_WET_LEVEL = 0.22f;
Depois:  private static final float REVERB_WET_LEVEL = 0.35f;

OU aumentar reverb gain:
Antes:   EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.55f);
Depois:  EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.70f);
```

### ❓ "Eco muito rápido/perto demais?"
```
SOLUÇÃO: Aumentar DELAY_BASE_TICKS
Antes:   private static final int DELAY_BASE_TICKS = 6;  // ~80ms
Depois:  private static final int DELAY_BASE_TICKS = 8;  // ~107ms

EFEITO: Reflexões parecem vir de mais longe
```

### ❓ "Eco muito lento/longe demais?"
```
SOLUÇÃO: Diminuir DELAY_BASE_TICKS
Antes:   private static final int DELAY_BASE_TICKS = 6;   // ~80ms
Depois:  private static final int DELAY_BASE_TICKS = 4;   // ~53ms

EFEITO: Reflexões parecem vir de mais perto
```

### ❓ "Som muito "boomy" (graves excessivos)?"
```
SOLUÇÃO 1: Aumentar AIR_ABSORPTION
Antes:   EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, 0.98f);
Depois:  EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, 0.99f);

SOLUÇÃO 2: Reduzir DECAY_HFRATIO (altas frequências decaem mais rápido)
Antes:   EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.80f);
Depois:  EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.70f);
```

### ❓ "Som muito fino/brilhante demais?"
```
SOLUÇÃO: Aumentar DECAY_HFRATIO (preserva mais altas frequências)
Antes:   EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.80f);
Depois:  EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.90f);
```

---

## 📊 Tabela de Impacto de Parâmetros

### ECHO_DECAY_FACTOR
```
Baixo (0.40)   → Echo desaparece MUITO rápido (som seco)
Médio (0.50)   → Desaparece rápido (natural, som limpo)
Atual (0.60)   → Moderado (natural)
Alto (0.75)    → Desaparece lento (mais ambient)
```
**Recomendação**: 0.50–0.65

### DELAY_BASE_TICKS
```
Baixo (3)      → ~40ms (muito perto)
Médio (6)      → ~80ms (ATUAL - natural)
Alto (9)       → ~120ms (mais distante)
Muito Alto (12) → ~160ms (muita reverberação)
```
**Recomendação**: 5–8

### REVERB_GAIN
```
Baixo (0.40)   → Reverb quase imperceptível
Médio (0.55)   → ATUAL (complementar)
Alto (0.70)    → Mais sensação de espaço
Muito Alto (0.85) → Domina, perde clareza
```
**Recomendação**: 0.45–0.70

### REVERB_DECAY_TIME
```
Curto (1.0)    → Sala pequena (clara, seca)
Médio (1.5)    → ATUAL (câmara natural)
Longo (2.5)    → Catedral (reverberante)
Muito Longo (4.0) → Cavernão (muito reverb)
```
**Recomendação**: 1.2–2.0

### REVERB_DECAY_HFRATIO
```
Baixo (0.60)   → Graves persistem mais (quente, boomy)
Médio (0.80)   → ATUAL (natural damping)
Alto (0.95)    → Altas frequências persistem (brilhante)
```
**Recomendação**: 0.70–0.90

---

## 🎨 Presets Recomendados

### **PRESET 1: Caverna Grande**
```java
private static final int DELAY_BASE_TICKS = 8;
private static final float ECHO_DECAY_FACTOR = 0.70f;
private static final float[] ECHO_VOLUME_BASE = { 0.75f, 0.50f };
private static final float REVERB_WET_LEVEL = 0.35f;

// Reverb
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.70f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 2.5f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, 1.25f);
```

### **PRESET 2: Sala Natural (Default)**
```java
// Configuração ATUAL ✅
private static final int DELAY_BASE_TICKS = 6;
private static final float ECHO_DECAY_FACTOR = 0.60f;
private static final float[] ECHO_VOLUME_BASE = { 0.65f, 0.40f };
private static final float REVERB_WET_LEVEL = 0.22f;

// Reverb (ATUAL)
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.55f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 1.5f);
```

### **PRESET 3: Sala Pequena (Seca)**
```java
private static final int DELAY_BASE_TICKS = 4;
private static final float ECHO_DECAY_FACTOR = 0.45f;
private static final float[] ECHO_VOLUME_BASE = { 0.45f, 0.25f };
private static final int NUM_ECHOS = 1; // Apenas 1 eco
private static final float REVERB_WET_LEVEL = 0.12f;

// Reverb
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.40f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 0.8f);
```

### **PRESET 4: Catedral/Espaço Vasto**
```java
private static final int DELAY_BASE_TICKS = 10;
private static final float ECHO_DECAY_FACTOR = 0.75f;
private static final float[] ECHO_VOLUME_BASE = { 0.80f, 0.55f };
private static final float REVERB_WET_LEVEL = 0.45f;

// Reverb
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.80f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 3.5f);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DIFFUSION, 0.90f);
```

---

## 🔧 Processo de Ajuste

1. **Identifique o problema** (veja "Diagnóstico" acima)
2. **Faça UMA mudança** apenas
3. **Compile**: `.\gradlew.bat build`
4. **Teste** na Creative Realm (ouvir 10-15 segundos)
5. **Se melhorou**: OK, próximo ajuste se necessário
6. **Se piorou**: Reverta e tente parâmetro diferente

**Tempo por ciclo**: ~10-15 segundos (build é rápido)

---

## 📝 Blank Template para Customização

Copie esta seção e preencha para seu preset:

```java
// === PRESET: [NOME] ===
// Adaptado para: [descrição do ambiente]

// Echo parameters
private static final int DELAY_BASE_TICKS = [valor];
private static final float ECHO_DECAY_FACTOR = [valor];
private static final float[] ECHO_VOLUME_BASE = { [v1], [v2] };
private static final int NUM_ECHOS = [valor];
private static final float REVERB_WET_LEVEL = [valor];
private static final float REVERB_DRY_LEVEL = [valor];

// Reverb parameters (ajuste conforme necessário)
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, [valor]);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, [valor]);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, [valor]);
EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, [valor]);
```

---

## ⚡ Dica Profissional

Para testar rapidamente **sem recompilar**, você pode criar uma classe de configuração separada que carrega de arquivo JSON em runtime. Exemplo:

```java
// future-mod-config.json (na pasta run/)
{
  "echo": {
    "delay_base_ticks": 6,
    "decay_factor": 0.60,
    "volume_base": [0.65, 0.40],
    "num_echos": 2,
    "reverb_dry": 0.98,
    "reverb_wet": 0.22,
    "reverb_gain": 0.55,
    "reverb_decay_time": 1.5
  }
}
```

Isto permitiria hot-reload de parâmetros sem compilar. Futura otimização! 🚀

---

## ✅ Quick Checklist

- [ ] Footsteps soam naturais
- [ ] Eco não mascara som original
- [ ] Ambiente percebido sem ser dominante
- [ ] Clareza mantida (sem boom)
- [ ] Profundidade presente
- [ ] Sem "tubo" ou excesso de reverb
- [ ] Performance normal (sem lag)

**Se tudo está bem**: ✅ **Deploy a produção**

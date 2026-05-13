from fastapi import FastAPI
import asyncpg

app = FastAPI()

DATABASE_URL = "postgresql://user:password@localhost:5432/rolematch_db"

@app.get("/api/v1/match")
async def get_matches(lat: float, lon: float, radius_km: int):
    # Conexão com o banco
    conn = await asyncpg.connect(DATABASE_URL)

    query = """
        SELECT id, nome, tags
        FROM restaurants
        WHERE ST_DWithin(
            geografia,
            ST_MakePoint($1, $2)::geography,
            $3 * 1000
        )
    """
    # Executa a busca passando longitude latitude e o raio em metros
    resultados = await conn.fetch(query, lon, lat, radius_km)

    await conn.close()

    # Aqui entraria o Algoritmo de IA para fazer o Score e devolver o Top 20
    # TODO: Implementar algoritmo futuro.
    return {"status": "sucesso", "data": resultados}
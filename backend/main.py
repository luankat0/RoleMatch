from fastapi import FastAPI, Body
from typing import List, Dict
from matcher import engine

app = FastAPI(title="RoleMatch AI Backend")

@app.get("/")
def read_root():
    return {"message": "RoleMatch AI Engine is running"}

@app.post("/recommend")
async def get_recommendations(
    payload: Dict = Body(...)
):
    """
    Endpoint que recebe:
     - user_profile: preferências e histórico do usuário
     - available_restaurants: lista de restaurantes da região
    """
    user_prefs = payload.get("user_profile", {})
    restaurants = payload.get("available_restaurants", [])

    # Engine processa e retorna os melhores matches
    recommendations = engine.calculate_match(user_prefs, restaurants)

    return {
        "user_id": user_prefs.get("id"),
        "total_found": len(recommendations),
        "matches": recommendations
    }
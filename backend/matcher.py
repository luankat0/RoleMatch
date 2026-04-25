import pandas as pd
from typing import List, Dict

class RoleMatchAI:
    def __init__(self):
        # TODO: Carregar modelo pré-treinado no futuro
        pass

    def calculate_match(self, user_preferences: Dict, restaurants: List[Dict]) -> List[Dict]:
        if not restaurants:
            return []
        
        df = pd.DataFrame(restaurants)
        
        # TODO: Atribuir pesos às métricas baseadas no comportamento do usuário
        # Ex: Se um usuário tem um "loyalty_score" alto em "comida_japonesa", esse peso aumenta.

        def score_row(row):
            score = 0
            # Validator: Preferência de Comida (Peso 50)
            if row['cuisine_type'] in user_preferences.get('favorite_cuisines', []):
                score += 50
            
            # Validator: Pet Friendly (Peso 30)
            if row['pet_friendly'] and user_preferences.get('needs_pet_friendly', False):
                score += 30

            # Validator: Faixa de Preço (Peso 20)
            if row['price_range'] == user_preferences.get('preferred_price', 2):
                score += 20

            return score
    
        df['match_score'] = df.apply(score_row, axis=1)

        df = df.sort_values(by='match_score', ascending=False)

        return df.to_dict(orient='records')

engine = RoleMatchAI()
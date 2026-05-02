
# ╔══════════════════════════════════════════════════════╗
# ║             SplitPay IVA — Control Panel             ║
# ╚══════════════════════════════════════════════════════╝

help:
	@echo "Comandos disponíveis:"
	@echo "  up       Sobe toda a stack (build e detach)"
	@echo "  down     Para e remove os containers"
	@echo "  restart  Reinicia os serviços"
	@echo "  build    Força o rebuild das imagens"
	@echo "  logs     Mostra logs de todos os serviços (ctrl+c para sair)"
	@echo "  ps       Lista o status dos containers"
	@echo "  clean    Remove containers, redes e volumes (limpeza total)"

up:
	docker compose up --build -d

down:
	docker compose down

restart:
	docker compose restart

build:
	docker compose build

logs:
	docker compose logs -f

ps:
	docker compose ps

clean:
	docker compose down -v --remove-orphans

import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Container } from "./Container";
import * as React from "react";

export function Header() {
  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
  };

  return (
    <header className="w-full h-16 border-b border-border/40 bg-background sticky top-0 z-10">
      <Container className="h-full flex items-center justify-between">
        <div className="flex items-center">
          {/* Logo */}
          <Link to="/" className="text-xl font-bold" onClick={handleClick}>
            Game Community
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center ml-16">
            <Link
              to="/"
              className="text-sm font-medium hover:text-primary transition-colors mr-8"
              onClick={handleClick}
            >
              홈
            </Link>
            <Link
              to="/games"
              className="text-sm font-medium text-foreground/70 hover:text-primary transition-colors mr-8"
              onClick={handleClick}
            >
              게임
            </Link>
            <Link
              to="/forums"
              className="text-sm font-medium text-foreground/70 hover:text-primary transition-colors mr-8"
              onClick={handleClick}
            >
              뉴스
            </Link>
            <Link
              to="/news"
              className="text-sm font-medium text-foreground/70 hover:text-primary transition-colors"
              onClick={handleClick}
            >
              게시판
            </Link>
          </nav>
        </div>

        {/* 로그인 버튼 */}
        <Button
          variant="outline"
          size="sm"
          className="font-medium"
          onClick={handleClick}
        >
          로그인
        </Button>
      </Container>
    </header>
  );
}

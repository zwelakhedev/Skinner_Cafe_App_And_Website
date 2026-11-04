using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SkinnerCafe.API.Data.Migrations
{
    /// <inheritdoc />
    public partial class AddCustomerProfilePictureUrl : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "ProfilePictureUrl",
                table: "CustomerProfiles",
                type: "nvarchar(max)",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "ProfilePictureUrl",
                table: "CustomerProfiles");
        }
    }
}
